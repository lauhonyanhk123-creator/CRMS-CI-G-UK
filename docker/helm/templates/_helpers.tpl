{{/*
Expand the name of the chart.
*/}}
{{- define "crms.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "crms.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create the namespace name.
*/}}
{{- define "crms.namespace" -}}
{{- .Values.namespace.name | default "crms" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "crms.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{ include "crms.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: {{ .Chart.Name }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "crms.selectorLabels" -}}
app.kubernetes.io/name: {{ include "crms.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the image pull secret name.
*/}}
{{- define "crms.imagePullSecret" -}}
{{- printf "%s-pull-secret" (include "crms.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
PostgreSQL fullname
*/}}
{{- define "crms.postgresql.fullname" -}}
{{- printf "%s-postgresql" (include "crms.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
MinIO fullname
*/}}
{{- define "crms.minio.fullname" -}}
{{- printf "%s-minio" (include "crms.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Backend fullname
*/}}
{{- define "crms.backend.fullname" -}}
{{- printf "%s-backend" (include "crms.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Frontend fullname
*/}}
{{- define "crms.frontend.fullname" -}}
{{- printf "%s-frontend" (include "crms.fullname" .) | trunc 63 | trimSuffix "-" }}
{{- end }}
