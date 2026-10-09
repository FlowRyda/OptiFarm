terraform {
  required_providers {
    google = {
      source = "hashicorp/google"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

resource "google_compute_address" "static_ip" {
  name   = "static-ip"
  region = var.region
}

resource "google_compute_firewall" "firewall" {
  network = "default"
  name    = "firewall"
  allow {
    protocol = "tcp"
    ports    = ["22", "80"]
  }
  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["optifarm"]
}

resource "google_compute_instance" "optifarm-vm" {
  name         = "optifarm-vm"
  machine_type = "e2-medium"
  zone         = var.zone
  tags         = ["optifarm"]

  boot_disk {
    initialize_params {
      image = "ubuntu-os-cloud/ubuntu-2404-lts-amd64"
    }
  }

  network_interface {
    network = "default"
    access_config {
      nat_ip = google_compute_address.static_ip.address # Assign the static IP to the instance
    }
  }

  metadata = {
    ssh-keys = "deploy:${file(var.ssh_public_key_path)}"
  }

  metadata_startup_script = file("${path.module}/startup.sh")
}

output "vm_ip" {
  value = google_compute_address.static_ip.address
}