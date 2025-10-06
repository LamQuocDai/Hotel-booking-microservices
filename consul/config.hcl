datacenter = "hotel-booking"
data_dir = "/consul/data"
log_level = "INFO"
server = true
bootstrap_expect = 1
ui_config {
  enabled = true
}
client_addr = "0.0.0.0"
retry_join = ["consul"]
ports {
  grpc = 8502
}
connect {
  enabled = true
}
