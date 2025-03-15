# Roadmap for Enhancing Database-Driven Security and Validation in ToDoList-App

## Phase 1: Requirements Gathering and Current State Review

### Review Current System
- Examine the existing Java app’s database interactions.
- Identify areas for improvement in validation, role management, and data handling.
- Review the current structure and roles in PostgreSQL to understand data access.

### Define Goals
- **Security**: Eliminate direct exposure of sensitive credentials in the Java app.
- **Validation**: Shift validation and business logic to PostgreSQL to ensure data integrity.
- **Synchronization**: Ensure seamless sync between local and the cloud PostgreSQL database.
- **Maintainability**: Facilitate easy updates and modifications to the database schema or logic.
- **UUIDs**: Utilize PostgreSQL extension for UUID v7 as primary keys to ensure unique and efficient identifiers.
- **JSON Handling**: Leverage JSON capabilities in PostgreSQL and Java to streamline data handling and validation.

## Phase 2: Database Schema, Roles, and Permissions Review/Modification

### Review Current Database Schema
- Examine current tables and data structures.
- List needed changes to improve validation, normalization, and data handling.

### Roles and Permissions
- **Create a New Role for the Java App**:
    - Grant necessary permissions (only the capability to send JSON files to a specific procedure).
- **Create Limited Access Roles**:
    - Define roles for administrative tasks (e.g., read-only roles for reporting).
- **Revoke Unnecessary Permissions**:   
    - Remove unnecessary permissions from default roles.

### Security: Database Credentials
- Implement secure handling of credentials using environment variables.
- Create a dedicated user/role with permissions to send and fetch data.
- Avoid direct exposure of sensitive credentials by using role-based access.

### PostgreSQL Functions for Validation and Insertion
- Create functions to handle task insertion with validation.
- Ensure only trusted users (like task_manager) can call these functions.

## Phase 3: Java App Adjustments

### Modify Java Database Functions
- Update methods to send JSON data for tasks instead of direct SQL queries.
- Use new PostgreSQL functions for data insertion and validation.

### Database Connection Handling in Java
- Set up a connection pool (e.g., HikariCP) with new role credentials (app_user).
- Ensure sensitive credentials are not exposed to the client.

### Sync with Cloud Database
- Develop a strategy for syncing local (JSON) and cloud (PostgreSQL) data.

### Error Handling
- Implement error handling for validation failures or sync errors.
- Log errors consistently and provide meaningful feedback to users.

## Phase 4: Synchronization and Validation Workflow

### Define Synchronization Workflow
- Outline data synchronization between cloud and local data.
- Define conflict resolution strategies for potential data conflicts.

### Data Validation During Sync
- Validate data on both sides during synchronization.

### Test Sync Scenarios
- Create test cases for online and offline scenarios.

## Phase 5: Implementation Plan and Timeline

### Small-Scale Testing
- Test new validation functions and roles on a small part of the system.

### Staged Rollout
- Proceed with a full rollout after small-scale validation.
- Update database schemas, roles, and functions as needed.
- Update Java app for new role-based access and validation mechanisms.

### Monitor and Debug
- Monitor the app for errors related to database interactions or sync issues.
- Be prepared to debug and adjust PostgreSQL functions, validation, and Java methods.

## Phase 6: Documentation

### Document Database Changes
- Document new roles, permissions, functions, and triggers in PostgreSQL.

### Document Java Changes
- Document changes to the Java app, especially around database connection handling, validation workflow, and synchronization process.

### Create Developer/Deployment Guides
- Prepare guides for setting up the app in different environments.
- Ensure developers know how to handle encrypted credentials and manage the sync process.

## Timeline Estimate
- **Phase 1**: 1 week
- **Phase 2**: 2-3 weeks
- **Phase 3**: 2-3 weeks
- **Phase 4**: 2-3 weeks
- **Phase 5**: 3-4 weeks
- **Phase 6**: 1 week
