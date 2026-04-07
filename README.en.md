# Anime Platform

A comprehensive anime content platform based on the Spring Cloud microservices architecture, supporting anime browsing, user management, video playback, real-time bullet comments, and more.

## Project Overview

This project is an anime content platform built on a microservices architecture, using Spring Cloud as the microservices framework, Nacos as the service registry, MySQL as the database, and Redis as the cache. The entire system consists of multiple microservice modules that collectively deliver complete anime platform functionality.

## Technology Stack

The backend is primarily built with Java 17 and the Spring Boot framework, including Spring Cloud Gateway as the API gateway, Spring Cloud OpenFeign for inter-service communication, and MyBatis Plus as the ORM framework. Data storage utilizes MySQL as the primary database and Redis for caching and session storage. Service registration and configuration management are handled by Alibaba Nacos. Deployment is implemented using Docker containerization and docker-compose for service orchestration.

## System Architecture

The project adopts a multi-service microservices architecture with clearly defined responsibilities for each module. The gateway module serves as the unified entry point, providing API gateway functionality including request routing, authentication, and rate limiting. The anime-service module is the core service for anime content management, offering CRUD operations, search, and recommendation features. The user-service module handles user management, including registration, login, and favorites management. The video-service module manages video content, providing video playback URLs and episode management. The crawler-service module is a data scraping service that automatically fetches anime data from external sources and synchronizes it to the database. The danmaku-service module enables real-time bullet comments via the WebSocket protocol for real-time message broadcasting.

## Core Features

The anime content management module supports multi-dimensional anime filtering by type, status, year, and style, and allows sorting by latest updates, popularity, and ratings. It also supports keyword search and search history analytics. The module provides automatic recommendations for popular titles and newly updated content, along with access statistics and user behavior logging.

The user management module offers full user account functionality, including registration, login verification (JWT Token supported), and favorites management (add, delete, query, check favorite status). The system also implements rate limiting for registration, restricting the maximum number of registrations per IP address per day.

The video playback module supports retrieving all episodes of an anime by its ID, fetching individual video details, and dynamically routing playback URLs.

The bullet comment module implements true real-time functionality using the WebSocket protocol, supporting joining and leaving rooms by video ID, broadcasting messages to specific rooms, and persisting bullet comments to the database automatically.

The crawler module automates data synchronization with support for incremental and full syncs. It can crawl data separately by region (Japan, USA, China), supports retry on failure, and automatically fills in missing cover images. Crawler tasks are scheduled to run periodically: Japanese anime is synced every 3 hours, while other regions are synced every 6 hours.

## Quick Start

### Environment Requirements

The deployment environment requires Java 17 or higher, Docker and docker-compose, and the Linux Ubuntu system (recommended: Ubuntu 20.04 or 22.04).

### Deployment Steps

First, clone the project code to your server:

```bash
git clone https://gitee.com/crazy-clown/anime-platform.git
cd anime-platform
```

Then configure environment variables by creating a `.env` file and setting required variables, including database passwords and version tags.

Start all services:

```bash
./deploy/deploy.sh
```

The first deployment requires building Docker images and may take considerable time. After startup, use `docker ps` to check service status.

### Service Ports

The system assigns the following ports: Gateway service listens on port 8080, anime-service on 8081, user-service on 8082, video-service on 8083, danmaku-service on 8084, crawler-service on 8085, MySQL database on 3306, Redis cache on 6379, and Nacos service registry and configuration center on 8848.

## API Interfaces

The platform provides rich RESTful APIs. Anime-related APIs include retrieving anime lists (with pagination and multiple filters), fetching anime details, deleting anime, publishing anime, searching anime, retrieving search statistics, fetching popular recommendations, retrieving latest updates, obtaining analytics, retrieving recent search history, and reporting access logs. User-related APIs include user registration, login, retrieving user information, fetching current user details, listing users, counting users, adding/removing favorites, retrieving favorite lists, and checking favorite status. Video-related APIs include fetching video lists by anime ID and retrieving individual video details. Crawler-related APIs include immediate crawling, full sync, incremental sync, region-specific sync, retrying failed tasks, retrieving failure counts, and filling missing cover images. Bullet comment APIs use WebSocket protocol; connect to `ws://host:8084/danmaku` and support message types: `join` (enter room), `leave` (exit room), and `send` (send bullet comment).

## Project Structure

The project uses a Maven multi-module structure, with the root `pom.xml` defining all submodules and dependency versions. The gateway module is the API gateway service. The anime-service module is the anime content management service. The user-service module is the user management service. The video-service module is the video content service. The crawler-service module is the data crawler service. The danmaku-service module is the bullet comment interaction service. The common module contains shared utilities and base classes. The deploy directory contains Docker deployment files and scripts.

## Notes

For production deployment, change default passwords, especially the database root password and JWT secret key. Only expose necessary ports to minimize security risks. Adjust JVM parameters according to server configuration for performance optimization. Regularly back up database data, preferably using cron jobs for automated backups. Default configurations are suitable for development and testing environments; adjust settings according to actual production requirements.

## License

This project is intended solely for learning and communication purposes. Do not use it for commercial purposes.