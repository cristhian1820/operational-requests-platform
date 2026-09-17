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
