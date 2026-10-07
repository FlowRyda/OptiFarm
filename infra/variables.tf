variable "project_id" {
  type = string
}

variable "region" {
  type    = string
  default = "europe-north2"
}

variable "zone" {
  type = string
  default = "europe-north2-b"
}

variable "ssh_public_key_path" {
  type    = string
  default = "~/.ssh/optifarm-deploy.pub"
}