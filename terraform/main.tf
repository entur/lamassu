terraform {
  required_version = ">= 0.13.2"
}

provider "google" {
  version = "~> 3.0"
  region  = var.gcp_region
}

provider "kubernetes" {
  version = "~> 1.13.3"
  load_config_file = var.load_config_file
}

resource "google_service_account" "service_account" {
  account_id   = "${var.labels.team}-${var.labels.app}-sa"
  display_name = "${var.labels.team}-${var.labels.app} service account"
  project = var.gcp_project
}

resource "google_service_account_key" "service_account_key" {
  service_account_id = google_service_account.service_account.name
}

resource "kubernetes_secret" "service_account_credentials" {
  metadata {
    name      = "${var.labels.team}-${var.labels.app}-sa-key"
    namespace = var.kube_namespace
  }
  data = {
    "credentials.json" = base64decode(google_service_account_key.service_account_key.private_key)
  }
}

resource "kubernetes_secret" "basic_auth_secret" {
  metadata {
    name      = "${var.labels.team}-${var.labels.app}-basic-auth"
    namespace = var.kube_namespace
  }

  data = {
    "lamassu.admin.password"     = var.ror-lamassu-admin-password
  }
}

module "redis" {
  source = "github.com/entur/terraform//modules/redis?ref=v0.0.23"
  gcp_project = var.redis_project
  labels = var.labels
  kubernetes_namespace = var.kube_namespace
  zone = var.redis_zone
  reserved_ip_range = var.redis_reserved_ip_range
  prevent_destroy = var.redis_prevent_destroy
}

# Reserve IP for cluster-internal service
resource "google_compute_address" "internal_service_address" {
  project      = var.vpc_project
  name         = var.internal_service_label
  subnetwork   = var.vpc_subnet
  address_type = "INTERNAL"
  address      = var.internal_service_ip
  region       = var.gcp_region
}