# Outputs do Terraform
# Esses valores são exportados após o terraform apply

output "instance_name" {
  description = "Nome da instância de VM criada"
  value       = google_compute_instance.todolist_server.name
}

output "instance_id" {
  description = "ID da instância de VM"
  value       = google_compute_instance.todolist_server.id
}

output "instance_zone" {
  description = "Zona onde a instância está rodando"
  value       = google_compute_instance.todolist_server.zone
}

output "public_ip" {
  description = "IP público da VM (usado para SSH e acesso à aplicação)"
  value       = google_compute_instance.todolist_server.network_interface[0].access_config[0].nat_ip
}

output "internal_ip" {
  description = "IP interno da VM"
  value       = google_compute_instance.todolist_server.network_interface[0].network_ip
}

output "ssh_connection" {
  description = "Comando para conectar via SSH"
  value       = "ssh ${var.ssh_user}@${google_compute_instance.todolist_server.network_interface[0].access_config[0].nat_ip}"
}

output "app_url" {
  description = "URL da aplicação"
  value       = "http://${google_compute_instance.todolist_server.network_interface[0].access_config[0].nat_ip}:8080"
}

output "health_check_url" {
  description = "URL do health check"
  value       = "http://${google_compute_instance.todolist_server.network_interface[0].access_config[0].nat_ip}:8080/actuator/health"
}
