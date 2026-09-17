from pathlib import Path
import sys

import yaml


ROOT = Path(__file__).resolve().parents[2]
PIPELINE = ROOT / ".gitlab-ci.yml"
REQUIRED_STAGES = {"validate", "test", "acceptance", "build", "security", "package"}
REQUIRED_JOBS = {
    "ci_yaml",
    "frontend_quality",
    "backend_tests",
    "helm_validate",
    "karate_acceptance",
    "docker_build",
    "security_scan",
    "docker_publish",
}


def assert_pipeline_contract(document: dict, text: str) -> None:
    helm_image = document["helm_validate"]["image"]
    trivy_image = document["security_scan"]["image"]
    if helm_image.get("name") != "alpine/helm:3.17.3" or helm_image.get("entrypoint") != [""]:
        raise ValueError("helm_validate must clear the Helm image entrypoint")
    if trivy_image.get("name") != "aquasec/trivy:0.58.1" or trivy_image.get("entrypoint") != [""]:
        raise ValueError("security_scan must clear the Trivy image entrypoint")
    workflow_rules = document.get("workflow", {}).get("rules", [])
    if not any(rule.get("if") == '$CI_PIPELINE_SOURCE == "merge_request_event"' for rule in workflow_rules):
        raise ValueError("workflow must allow merge request pipelines")
    if not any(rule.get("if") == '$CI_PIPELINE_SOURCE == "push" && $CI_OPEN_MERGE_REQUESTS' and rule.get("when") == "never" for rule in workflow_rules):
        raise ValueError("workflow must suppress branch pipelines when an MR is open")
    if not any(rule.get("if") == '$CI_COMMIT_TAG' for rule in workflow_rules):
        raise ValueError("workflow must allow tag pipelines")
    if not any(rule.get("if") == '$CI_PIPELINE_SOURCE == "push" && $CI_COMMIT_BRANCH' for rule in workflow_rules):
        raise ValueError("workflow must allow branch push pipelines")
    if workflow_rules[-1].get("when") != "never":
        raise ValueError("workflow must end with when: never")
    karate_section = text.split("\nkarate_acceptance:", 1)[1].split("\ndocker_build:", 1)[0]
    if "localhost:" in karate_section:
        raise ValueError("CI pipeline must not use localhost ports for DinD acceptance checks")
    required_network_markers = (
        'docker inspect --format',
        'docker run --rm --network "$COMPOSE_NETWORK" curlimages/curl:8.11.1',
        'docker run --name karate-runner-$CI_PIPELINE_ID --network "$COMPOSE_NETWORK"',
        '-Drequests.api.url=http://requests-service:8081',
        '-Dkeycloak.url=http://keycloak:8080',
    )
    missing_markers = [marker for marker in required_network_markers if marker not in text]
    if missing_markers:
        raise ValueError(f"DinD Karate network strategy incomplete: {missing_markers}")


def main() -> int:
    document = yaml.safe_load(PIPELINE.read_text(encoding="utf-8"))
    if not isinstance(document, dict):
        raise ValueError(".gitlab-ci.yml must contain a YAML mapping")
    stages = set(document.get("stages", []))
    missing_stages = REQUIRED_STAGES - stages
    if missing_stages:
        raise ValueError(f"missing stages: {sorted(missing_stages)}")
    missing_jobs = REQUIRED_JOBS - set(document)
    if missing_jobs:
        raise ValueError(f"missing jobs: {sorted(missing_jobs)}")
    paths = [
        ROOT / "frontend/package-lock.json",
        ROOT / "pom.xml",
        ROOT / "tests/karate",
        ROOT / "infrastructure/helm/operational-requests",
        ROOT / "backend/requests-service/Dockerfile",
        ROOT / "backend/indicators-service/Dockerfile",
        ROOT / "frontend/shell/Dockerfile",
        ROOT / "frontend/requests-mfe/Dockerfile",
    ]
    missing_paths = [str(path.relative_to(ROOT)) for path in paths if not path.exists()]
    if missing_paths:
        raise ValueError(f"referenced paths do not exist: {missing_paths}")
    text = PIPELINE.read_text(encoding="utf-8")
    assert_pipeline_contract(document, text)
    forbidden = ["-----BEGIN", "eyJhbGci", "client_secret:", "refresh_token:"]
    found = [pattern for pattern in forbidden if pattern in text]
    if found:
        raise ValueError(f"forbidden secret-like content found: {found}")
    print(f"Validated YAML, {len(REQUIRED_JOBS)} jobs, {len(stages)} stages, no secret-like literals")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError, yaml.YAMLError) as error:
        print(f"CI validation failed: {error}", file=sys.stderr)
        raise SystemExit(1)
