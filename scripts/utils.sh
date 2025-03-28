#!/bin/bash

# filepath: c:\Dev\Docker\DevEnvs\Java\ToDoListApp\scripts\test_app.sh

BASE_DIRECTORY="$HOME/.todoapp"

# Function to ensure the base directory exists
ensure_base_directory_exists() {
    if [ ! -d "$BASE_DIRECTORY" ]; then
        mkdir -p "$BASE_DIRECTORY"
        if [ $? -eq 0 ]; then
            echo "Created base directory: $BASE_DIRECTORY"
        else
            echo "Failed to create base directory: $BASE_DIRECTORY" >&2
            exit 1
        fi
    fi
}

print_json_file() {
    local json_file="$1"
    if [ -f "$json_file" ]; then
        cat "$json_file"
    else
        echo "Error: JSON file not found: $json_file" >&2
        exit 1
    fi
}

# Main script logic
ensure_base_directory_exists

if [ $# -eq 0 ]; then
    echo "Usage: $0 <json-file-name>"
    exit 1
fi

JSON_FILE_PATH="$BASE_DIRECTORY/$1"

if [ -f "$JSON_FILE_PATH" ]; then
    echo "Contents of JSON file ($JSON_FILE_PATH):"
    print_json_file "$JSON_FILE_PATH"
else
    echo "No JSON file found at $JSON_FILE_PATH. Provide a valid file name."
    exit 1
fi