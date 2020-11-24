variable "gcp_project" {
  description = "The GCP project id"
}

variable "gcp_region" {
  description = "The GCP region"
  default     = "europe-west1"
}

variable "kube_namespace" {
  description = "The Kubernetes namespace"
}

variable "labels" {
  description = "Labels used in all resources"
  type        = map(string)
  default = {
    manager = "terraform"
    team    = "ror"
    slack   = "talk-ror"
    app     = "lamassu"
  }
}
variable "load_config_file" {
  description = "Do not load kube config file"
  default     = false
}

variable "redis_project" {
  description = "The GCP project for redis"
}

variable "redis_zone" {
  description = "The GCP zone for redis"
  default = "europe-west1-d"
}

variable "redis_reserved_ip_range" {
  description = "IP range for Redis, follow addressing scheme"
}

variable "redis_prevent_destroy" {
  description = "Prevents destruction of this redis instance"
  type        = bool
  default     = false
}
