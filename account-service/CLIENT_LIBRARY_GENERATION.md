# gRPC Client Library Generation Guide

This guide explains how to generate client libraries for different programming languages to communicate with the Account Service gRPC server.

## Overview

The `account.proto` file defines the gRPC service contracts. Using the Protocol Buffer compiler and gRPC plugins, you can generate client stubs for any supported language.

## Supported Languages

gRPC supports the following languages:

- Java ✅ (Built into Maven)
- Python ✅ (pip)
- Node.js ✅ (npm)
- Go ✅ (official)
- C++ ✅ (official)
- C# ✅ (official)
- Ruby ✅ (official)
- PHP ✅ (official)
- Kotlin ✅ (official)
- TypeScript ✅ (via ts-Protoc plugin)

## Prerequisites

Install the Protocol Buffer compiler:

### macOS (Homebrew)

```bash
brew install protobuf
```

### Ubuntu/Debian

```bash
apt-get install protobuf-compiler
```

### Windows (Chocolatey)

```bash
choco install protoc
```

### Verify Installation

```bash
protoc --version
# Should output: libprotoc X.X.X
```

## Java Client Library

### Already Included

Java client library generation is **automatically handled by Maven**:

```bash
mvn clean package
```

**Generated Files Location**:

- `target/generated-sources/protobuf/java/` - Message classes
- `target/generated-sources/protobuf/grpc-java/` - Service stubs

**Usage in Your Project**:

Add to `pom.xml`:

```xml
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>1.60.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>1.60.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>1.60.0</version>
</dependency>
```

## Python Client Library

### Installation

```bash
pip install grpcio grpcio-tools
```

### Generate Client Stubs

From the project root:

```bash
python -m grpc_tools.protoc \
  -I./src/main/proto \
  --python_out=. \
  --grpc_python_out=. \
  src/main/proto/account.proto
```

This generates:

- `account_pb2.py` - Message classes
- `account_pb2_grpc.py` - Service stubs

### Python Client Example

```python
import grpc
from account_pb2 import LoginRequest
from account_pb2_grpc import AuthServiceStub

# Create channel
channel = grpc.insecure_channel('localhost:50051')
stub = AuthServiceStub(channel)

# Call Login
request = LoginRequest(email='user@example.com', password='pass123')
response = stub.Login(request)

print(f"Token: {response.token}")
print(f"User: {response.user.username}")

channel.close()
```

### Published Package (Optional)

Create `setup.py` to publish to PyPI:

```python
from setuptools import setup

setup(
    name='account-service-client',
    version='1.0.0',
    description='gRPC client for Hotel Booking Account Service',
    py_modules=['account_pb2', 'account_pb2_grpc'],
    install_requires=['grpcio'],
)
```

## Node.js Client Library

### Installation

```bash
npm install @grpc/grpc-js @grpc/proto-loader
```

### Generate Client Stubs

```bash
npm install --save-dev @grpc/proto-loader
```

### Node.js Client Example

```javascript
const grpc = require("@grpc/grpc-js");
const protoLoader = require("@grpc/proto-loader");

// Load proto file
const packageDef = protoLoader.loadSync("./account.proto", {});
const proto = grpc.loadPackageDefinition(packageDef).com.hotelbooking.account;

// Create channel and client
const channel = new grpc.credentials.createInsecure();
const client = new proto.AuthService("localhost:50051", channel);

// Call Login
client.Login(
  { email: "user@example.com", password: "pass123" },
  (err, response) => {
    if (err) {
      console.error("Error:", err);
    } else {
      console.log("Token:", response.token);
      console.log("User:", response.user.username);
    }
  },
);
```

### TypeScript Client

Install types:

```bash
npm install --save-dev typescript ts-protoc-gen
```

Generate with TypeScript support:

```bash
npm install --save-dev grpc-tools ts-protoc-gen

npx grpc_tools_node_protoc \
  --js_out=import_style=commonjs,binary:./src \
  --grpc_out=grpc_js:./src \
  --plugin=protoc-gen-ts=./node_modules/.bin/protoc-gen-ts \
  --ts_out=grpc_js:./src \
  src/account.proto
```

## Go Client Library

### Installation

```bash
go get github.com/grpc/grpc-go
go get google.golang.org/grpc/cmd/protoc-gen-go-grpc
go get google.golang.org/protobuf/cmd/protoc-gen-go
```

### Generate Client Stubs

```bash
protoc \
  --go_out=. \
  --go-grpc_out=. \
  --go_opt=module=github.com/your-org/account-service \
  --go-grpc_opt=module=github.com/your-org/account-service \
  src/main/proto/account.proto
```

### Go Client Example

```go
package main

import (
    "context"
    "fmt"
    "log"
    pb "github.com/your-org/account-service/proto"
    "google.golang.org/grpc"
)

func main() {
    conn, err := grpc.Dial("localhost:50051", grpc.WithInsecure())
    if err != nil {
        log.Fatalf("did not connect: %v", err)
    }
    defer conn.Close()

    c := pb.NewAuthServiceClient(conn)

    resp, err := c.Login(context.Background(), &pb.LoginRequest{
        Email:    "user@example.com",
        Password: "pass123",
    })

    if err != nil {
        log.Fatalf("could not login: %v", err)
    }

    fmt.Println("Token:", resp.Token)
    fmt.Println("User:", resp.User.Username)
}
```

## C# Client Library

### Installation

```bash
dotnet add package Grpc.Client
dotnet add package Google.Protobuf
dotnet add package Grpc.Tools
```

### Generate Client Stubs

Add to `.csproj`:

```xml
<ItemGroup>
  <Protobuf Include="account.proto" GrpcServices="Client" />
</ItemGroup>
```

Build the project:

```bash
dotnet build
```

### C# Client Example

```csharp
using Grpc.Net.Client;
using AccountService;

class Program {
    static async Task Main(string[] args) {
        var channel = GrpcChannel.ForAddress("http://localhost:50051");
        var client = new AuthService.AuthServiceClient(channel);

        var reply = await client.LoginAsync(new LoginRequest {
            Email = "user@example.com",
            Password = "pass123"
        });

        Console.WriteLine($"Token: {reply.Token}");
        Console.WriteLine($"User: {reply.User.Username}");
    }
}
```

## Ruby Client Library

### Installation

```bash
gem install grpc
gem install grpc-tools
```

### Generate Client Stubs

```bash
grpc_tools_ruby_protoc \
  -I ./src/main/proto \
  --ruby_out=./lib \
  --grpc_out=./lib \
  src/main/proto/account.proto
```

### Ruby Client Example

```ruby
require 'grpc'
require_relative 'account_pb'
require_relative 'account_services_pb'

stub = Account::AuthService::Stub.new('localhost:50051', :this_channel_is_insecure)

response = stub.login(
  Account::LoginRequest.new(
    email: 'user@example.com',
    password: 'pass123'
  )
)

puts "Token: #{response.token}"
puts "User: #{response.user.username}"
```

## PHP Client Library

### Installation

```bash
composer require grpc/grpc
composer require google/protobuf
```

### Generate Client Stubs

### PHP Client Example

```php
<?php
require 'vendor/autoload.php';

use Com\Hotelbooking\Account\LoginRequest;
use Com\Hotelbooking\Account\AuthServiceClient;

$client = new AuthServiceClient('localhost:50051', [
    'credentials' => Grpc\ChannelCredentials::createInsecure(),
]);

$request = new LoginRequest();
$request->setEmail('user@example.com');
$request->setPassword('pass123');

list($response, $status) = $client->Login($request)->wait();

echo "Token: " . $response->getToken() . "\n";
echo "User: " . $response->getUser()->getUsername() . "\n";
```

## API Gateway / REST Fallback

If you need REST API compatibility during migration, consider using **gRPC-JSON Transcoding**:

### gRPC Gateway (Go)

```bash
go install github.com/grpc-ecosystem/grpc-gateway/v2/cmd/protoc-gen-grpc-gateway@latest
go install github.com/grpc-ecosystem/grpc-gateway/v2/cmd/protoc-gen-openapiv2@latest
```

Update proto:

```protobuf
import "google/api/annotations.proto";
import "google/api/client.proto";

service AuthService {
  rpc Login(LoginRequest) returns (LoginResponse) {
    option (google.api.http) = {
      post: "/v1/auth/login"
      body: "*"
    };
  };
}
```

Generate gateway:

```bash
protoc -I . \
  --grpc-gateway_out=. \
  --openapiv2_out=. \
  account.proto
```

### Envoy Proxy Transcoding

Configure Envoy to transcode REST calls to gRPC:

```yaml
listeners:
  - name: listener_0
    address:
      socket_address:
        address: 0.0.0.0
        port_number: 8080
    filter_chains:
      - filters:
          - name: envoy.filters.network.http_connection_manager
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
              codec_type: AUTO
              stat_prefix: ingress_http
              route_config:
                name: local_route
                virtual_hosts:
                  - name: account_service
                    domains: ["*"]
                    routes:
                      - match:
                          prefix: "/"
                        route:
                          cluster: grpc_backend
                          timeout: 30s

clusters:
  - name: grpc_backend
    connect_timeout: 1s
    type: STRICT_DNS
    lb_policy: ROUND_ROBIN
    load_assignment:
      cluster_name: grpc_backend
      endpoints:
        - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: localhost
                    port_number: 50051
    h2_upgrade_policy: UPGRADE
    upstream_protocol_options: {}
```

## Continuous Integration

### Maven (Java)

```yaml
# .github/workflows/build.yml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: "17"
      - run: mvn clean package
      - uses: actions/upload-artifact@v2
        with:
          name: generated-clients
          path: target/generated-sources/
```

### GitHub Actions (Multi-Language)

```yaml
name: Generate Clients

on: [push]

jobs:
  generate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up protoc
        run: |
          sudo apt-get update
          sudo apt-get install -y protobuf-compiler
      - name: Generate Java
        run: mvn clean compile
      - name: Generate Python
        run: |
          pip install grpcio-tools
          python -m grpc_tools.protoc -I./src/main/proto --python_out=. --grpc_python_out=. src/main/proto/account.proto
      - name: Generate Node.js
        run: npm run generate
      - name: Upload artifacts
        uses: actions/upload-artifact@v2
```

## Publishing Client Libraries

### Java (Maven Central)

See Maven Central publishing guidelines.

### Python (PyPI)

```bash
python setup.py sdist bdist_wheel
twine upload dist/*
```

### Node.js (npm)

```bash
npm login
npm publish
```

### Go (GitHub)

Push to GitHub with version tags:

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Documentation Generation

### gRPC Reflection

Enable service reflection (optional):

```java
// In GrpcConfig.java
serverBuilder.addService(ProtoReflectionService.newInstance());
```

This allows tools like grpcurl to discover services without proto file.

### OpenAPI/Swagger Documentation

If using REST transcoding, generate OpenAPI docs:

```bash
# With grpc-gateway
protoc -I . --openapiv2_out=./openapi account.proto
```

---

## Troubleshooting

### Import Error: Cannot find account.proto

Solution: Specify correct `-I` flag pointing to proto directory:

```bash
protoc -I./src/main/proto --python_out=. src/main/proto/account.proto
```

### protoc Version Mismatch

Solution: Update protoc:

```bash
protoc --version
# Update as needed
```

### Generated Code Location

All tools generate in different default locations. Specify output with `-*_out` flags.

---

For more examples, see:

- [gRPC Official Examples](https://github.com/grpc/grpc/tree/master/examples)
- [Account Service GRPC_MIGRATION_GUIDE.md](./GRPC_MIGRATION_GUIDE.md)
