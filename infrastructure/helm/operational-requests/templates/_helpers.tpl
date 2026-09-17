{{- define "operational-requests.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- define "operational-requests.fullname" -}}
{{- if .Values.fullnameOverride }}{{ .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}{{ else }}{{ printf "%s-%s" .Release.Name (include "operational-requests.name" .) | trunc 63 | trimSuffix "-" }}{{ end }}
{{- end }}
{{- define "operational-requests.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
app.kubernetes.io/name: {{ include "operational-requests.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
{{- define "operational-requests.selectorLabels" -}}
app.kubernetes.io/name: {{ include "operational-requests.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
{{- define "operational-requests.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}{{ default (include "operational-requests.fullname" .) .Values.serviceAccount.name }}{{ else }}{{ default "default" .Values.serviceAccount.name }}{{ end }}
{{- end }}
{{- define "operational-requests.secretName" -}}
{{- default (printf "%s-runtime" (include "operational-requests.fullname" .)) .Values.secrets.existingSecret }}
{{- end }}
{{- define "operational-requests.image" -}}
{{ .repository }}:{{ .tag }}
{{- end }}
