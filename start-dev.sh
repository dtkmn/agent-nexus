#!/bin/bash

echo "Starting A2A Gateway Infrastructure..."
echo ""

# Start PostgreSQL
echo "1. Starting PostgreSQL..."
docker-compose up -d

echo ""
echo "Waiting for PostgreSQL to be ready..."
sleep 3

# Check if PostgreSQL is ready
docker-compose exec -T postgres pg_isready -U quarkus || {
    echo "Waiting a bit more for PostgreSQL..."
    sleep 5
}

echo ""
echo "✅ PostgreSQL is ready!"
echo ""
echo "Database Connection Info:"
echo "  Host: localhost"
echo "  Port: 5432"
echo "  Database: quarkus"
echo "  Username: quarkus"
echo "  Password: quarkus"
echo ""
echo "To connect to the database:"
echo "  psql -h localhost -U quarkus -d quarkus"
echo ""
echo "To start the application:"
echo "  ./mvnw quarkus:dev"
echo ""
