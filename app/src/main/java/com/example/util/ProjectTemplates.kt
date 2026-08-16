package com.example.util

import com.example.data.model.ProjectEntity
import com.example.data.model.WorkspaceFileEntity
import java.util.UUID

object ProjectTemplates {

    fun getDefaultProjects(): List<Pair<ProjectEntity, List<WorkspaceFileEntity>>> {
        val nextJsProjectId = UUID.randomUUID().toString()
        val fastapiProjectId = UUID.randomUUID().toString()
        val composeProjectId = UUID.randomUUID().toString()
        val goProjectId = UUID.randomUUID().toString()
        val flutterProjectId = UUID.randomUUID().toString()
        val reactNativeProjectId = UUID.randomUUID().toString()
        val springProjectId = UUID.randomUUID().toString()
        val rustProjectId = UUID.randomUUID().toString()

        val nextJsProject = ProjectEntity(
            id = nextJsProjectId,
            name = "OpenCode SaaS Studio",
            description = "Fullstack Next.js 15, React Server Actions, TypeScript & Tailwind CSS",
            templateType = "nextjs",
            agentsRules = """
                # OpenCode SaaS Project Rules
                1. Always use TypeScript strict mode.
                2. Use Tailwind CSS utility classes for styling.
                3. Keep API routes idempotent and protected.
                4. Write tests for API handlers before committing changes.
            """.trimIndent()
        )

        val nextJsFiles = listOf(
            WorkspaceFileEntity(
                projectId = nextJsProjectId,
                filePath = "package.json",
                language = "json",
                content = """
                    {
                      "name": "opencode-saas-studio",
                      "version": "1.0.0",
                      "private": true,
                      "scripts": {
                        "dev": "next dev",
                        "build": "next build",
                        "start": "next start",
                        "lint": "next lint",
                        "test": "jest --passWithNoTests"
                      },
                      "dependencies": {
                        "next": "^15.1.0",
                        "react": "^19.0.0",
                        "react-dom": "^19.0.0",
                        "lucide-react": "^0.470.0",
                        "tailwind-merge": "^2.6.0"
                      },
                      "devDependencies": {
                        "typescript": "^5.7.2",
                        "@types/node": "^22.10.2",
                        "@types/react": "^19.0.2",
                        "tailwindcss": "^3.4.17",
                        "jest": "^29.7.0"
                      }
                    }
                """.trimIndent()
            ),
            WorkspaceFileEntity(
                projectId = nextJsProjectId,
                filePath = "src/app/page.tsx",
                language = "typescript",
                content = """
                    import React from 'react';
                    import { Terminal, Cpu, Play, GitBranch, Layers } from 'lucide-react';

                    export default function HomePage() {
                      return (
                        <main className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center justify-center p-6">
                          <div className="max-w-4xl w-full border border-slate-800 rounded-xl bg-slate-900/60 p-8 shadow-2xl backdrop-blur-md">
                            <div className="flex items-center gap-3 border-b border-slate-800 pb-4 mb-6">
                              <Terminal className="w-7 h-7 text-sky-400" />
                              <h1 className="text-2xl font-mono font-bold">OpenCode Autonomous Studio</h1>
                              <span className="ml-auto text-xs px-2.5 py-1 rounded bg-sky-500/20 text-sky-300 font-mono">v1.0.0</span>
                            </div>

                            <p className="text-slate-400 mb-6">
                              Welcome to the autonomous programming workspace. OpenCode executes multi-file edits, runs linter validations, and tracks Git snapshots.
                            </p>

                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                              <div className="p-4 rounded-lg bg-slate-800/50 border border-slate-700/50 flex flex-col gap-2">
                                <Layers className="w-5 h-5 text-emerald-400" />
                                <h3 className="font-semibold text-sm">Plan & Build Modes</h3>
                                <p className="text-xs text-slate-400">Architect blueprint steps before applying atomic file updates.</p>
                              </div>
                              <div className="p-4 rounded-lg bg-slate-800/50 border border-slate-700/50 flex flex-col gap-2">
                                <GitBranch className="w-5 h-5 text-purple-400" />
                                <h3 className="font-semibold text-sm">Git Snapshots</h3>
                                <p className="text-xs text-slate-400">1-click undo/redo and live unified diffs across all files.</p>
                              </div>
                              <div className="p-4 rounded-lg bg-slate-800/50 border border-slate-700/50 flex flex-col gap-2">
                                <Cpu className="w-5 h-5 text-amber-400" />
                                <h3 className="font-semibold text-sm">LSP Diagnostics</h3>
                                <p className="text-xs text-slate-400">Automated compiler feedback loops directly into the AI agent.</p>
                              </div>
                            </div>
                          </div>
                        </main>
                      );
                    }
                """.trimIndent()
            ),
            WorkspaceFileEntity(
                projectId = nextJsProjectId,
                filePath = "src/app/api/agent/route.ts",
                language = "typescript",
                content = """
                    import { NextResponse } from 'next/server';

                    export async function POST(request: Request) {
                      try {
                        const body = await request.json();
                        const { prompt, mode = 'BUILD' } = body;

                        if (!prompt) {
                          return NextResponse.json({ error: 'Prompt is required' }, { status: 400 });
                        }

                        // OpenCode Agent Autonomous Pipeline
                        const response = {
                          mode,
                          status: 'COMPLETED',
                          plan: mode === 'PLAN' ? [
                            'Analyze project schema and dependencies',
                            'Generate component interface',
                            'Integrate state management',
                            'Run unit test suite'
                          ] : null,
                          toolCalls: [
                            { tool: 'readFile', path: 'src/app/page.tsx' },
                            { tool: 'lintProject', diagnostics: 0 }
                          ]
                        };

                        return NextResponse.json(response);
                      } catch (err: any) {
                        return NextResponse.json({ error: err.message }, { status: 500 });
                      }
                    }
                """.trimIndent()
            ),
            WorkspaceFileEntity(
                projectId = nextJsProjectId,
                filePath = "AGENTS.md",
                language = "markdown",
                content = """
                    # AGENTS.md

                    ## Autonomous Agent Rules
                    - Maintain strict adherence to Next.js App Router patterns.
                    - Always execute `npm test` after modifying API routes.
                    - Commit clean snapshots for every atomic logical feature.
                """.trimIndent()
            ),
            WorkspaceFileEntity(
                projectId = nextJsProjectId,
                filePath = "README.md",
                language = "markdown",
                content = """
                    # OpenCode SaaS Studio

                    An autonomous coding environment powered by OpenCode.
                    
                    ## Getting Started
                    - `npm run dev` to start dev server
                    - `npm test` to run test suites
                    - Switch to **Plan Mode** to blueprint major architectural refactors.
                """.trimIndent()
            )
        )

        val fastApiProject = ProjectEntity(
            id = fastapiProjectId,
            name = "FastAPI AI Engine",
            description = "High-performance Python backend with Pydantic v2 and Async Coroutines",
            templateType = "fastapi",
            agentsRules = """
                # Python Guidelines
                - Follow PEP 8 and use Type Hints on all functions.
                - Use async def for I/O operations.
                - Ensure pytest pass rate before finishing tasks.
            """.trimIndent()
        )

        val fastApiFiles = listOf(
            WorkspaceFileEntity(
                projectId = fastapiProjectId,
                filePath = "main.py",
                language = "python",
                content = """
                    from fastapi import FastAPI, HTTPException
                    from pydantic import BaseModel
                    from typing import List, Optional

                    app = FastAPI(title="OpenCode AI Engine", version="1.0.0")

                    class TaskRequest(BaseModel):
                        prompt: str
                        mode: str = "BUILD"
                        context_files: Optional[List[str]] = []

                    class TaskResponse(BaseModel):
                        task_id: str
                        status: str
                        summary: str

                    @app.get("/")
                    async def root():
                        return {"status": "online", "engine": "OpenCode-Autonomous-Core"}

                    @app.post("/agent/execute", response_model=TaskResponse)
                    async def execute_task(req: TaskRequest):
                        if not req.prompt:
                            raise HTTPException(status_code=400, detail="Empty prompt")
                        return TaskResponse(
                            task_id="task_99182",
                            status="COMPLETED",
                            summary=f"Processed '{req.prompt}' in {req.mode} mode successfully."
                        )
                """.trimIndent()
            ),
            WorkspaceFileEntity(
                projectId = fastapiProjectId,
                filePath = "tests/test_main.py",
                language = "python",
                content = """
                    import pytest
                    from fastapi.testclient import TestClient
                    from main import app

                    client = TestClient(app)

                    def test_root():
                        response = client.get("/")
                        assert response.status_code == 200
                        assert response.json()["status"] == "online"

                    def test_execute():
                        response = client.post("/agent/execute", json={"prompt": "Refactor auth model"})
                        assert response.status_code == 200
                        assert response.json()["status"] == "COMPLETED"
                """.trimIndent()
            ),
            WorkspaceFileEntity(
                projectId = fastapiProjectId,
                filePath = "requirements.txt",
                language = "bash",
                content = """
                    fastapi>=0.115.0
                    uvicorn[standard]>=0.32.0
                    pydantic>=2.10.0
                    pytest>=8.3.0
                    httpx>=0.28.0
                """.trimIndent()
            ),
            WorkspaceFileEntity(
                projectId = fastapiProjectId,
                filePath = "AGENTS.md",
                language = "markdown",
                content = """
                    # AGENTS.md for FastAPI
                    - Run `pytest` before completing any ticket.
                    - Always use Pydantic models for request & response schemas.
                """.trimIndent()
            )
        )

        // 3. Go Backend Service
        val goProject = ProjectEntity(
            id = goProjectId,
            name = "Go Cloud Microservice",
            description = "High-throughput Go 1.23 REST API with Gin, goroutines, and JSON validation",
            templateType = "go",
            agentsRules = """
                # Go Microservice Rules
                - Handle all errors explicitly (`if err != nil`).
                - Keep structs well-annotated with JSON tags.
                - Run `go test ./...` on every PR.
            """.trimIndent()
        )
        val goFiles = getFilesForNewProject(goProjectId, "Go Cloud Microservice", "High-throughput Go REST API", "go")

        // 4. Flutter Multiplatform App
        val flutterProject = ProjectEntity(
            id = flutterProjectId,
            name = "Flutter Cross-Platform Studio",
            description = "Flutter 3.27 & Dart mobile workspace with clean state management",
            templateType = "flutter",
            agentsRules = """
                # Flutter & Dart Rules
                - Prefer const constructors where possible.
                - Separate UI widgets from business logic providers.
                - Run `flutter test` for widget coverage.
            """.trimIndent()
        )
        val flutterFiles = getFilesForNewProject(flutterProjectId, "Flutter Cross-Platform Studio", "Flutter 3.27 mobile workspace", "flutter")

        // 5. React Native Expo
        val reactNativeProject = ProjectEntity(
            id = reactNativeProjectId,
            name = "React Native Expo Mobile",
            description = "Universal iOS & Android app with TypeScript, Expo Router, and StyleSheet",
            templateType = "reactnative",
            agentsRules = """
                # React Native Expo Rules
                - Use React Native StyleSheet or NativeWind.
                - Handle mobile safe area insets properly.
            """.trimIndent()
        )
        val reactNativeFiles = getFilesForNewProject(reactNativeProjectId, "React Native Expo Mobile", "Universal iOS & Android app", "reactnative")

        // 6. Spring Boot Kotlin
        val springProject = ProjectEntity(
            id = springProjectId,
            name = "Spring Boot Cloud Core",
            description = "Enterprise Kotlin Spring Boot 3.4 microservice with REST and JPA",
            templateType = "springboot",
            agentsRules = """
                # Spring Boot Rules
                - Use Spring Data repositories and constructor injection.
                - Use Kotlin data classes for DTOs.
            """.trimIndent()
        )
        val springFiles = getFilesForNewProject(springProjectId, "Spring Boot Cloud Core", "Enterprise Kotlin Spring Boot microservice", "springboot")

        return listOf(
            Pair(nextJsProject, nextJsFiles),
            Pair(fastApiProject, fastApiFiles),
            Pair(goProject, goFiles),
            Pair(flutterProject, flutterFiles),
            Pair(reactNativeProject, reactNativeFiles),
            Pair(springProject, springFiles)
        )
    }

    fun getFilesForNewProject(
        projectId: String,
        name: String,
        description: String,
        templateType: String
    ): List<WorkspaceFileEntity> {
        return when (templateType.lowercase()) {
            "go" -> listOf(
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "main.go",
                    language = "go",
                    content = """
                        package main

                        import (
                        	"net/http"
                        	"github.com/gin-gonic/gin"
                        )

                        type TaskRequest struct {
                        	Title       string `json:"title" binding:"required"`
                        	Description string `json:"description"`
                        }

                        type TaskResponse struct {
                        	ID     string `json:"id"`
                        	Status string `json:"status"`
                        }

                        func main() {
                        	r := gin.Default()

                        	r.GET("/health", func(c *gin.Context) {
                        		c.JSON(http.StatusOK, gin.H{
                        			"status": "healthy",
                        			"service": "$name",
                        		})
                        	})

                        	r.POST("/api/tasks", func(c *gin.Context) {
                        		var req TaskRequest
                        		if err := c.ShouldBindJSON(&req); err != nil {
                        			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
                        			return
                        		}
                        		c.JSON(http.StatusCreated, TaskResponse{
                        			ID:     "task_go_101",
                        			Status: "created",
                        		})
                        	})

                        	r.Run(":8080")
                        }
                    """.trimIndent()
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "main_test.go",
                    language = "go",
                    content = """
                        package main

                        import (
                        	"testing"
                        )

                        func TestTaskResponse(t *testing.T) {
                        	resp := TaskResponse{ID: "1", Status: "ok"}
                        	if resp.Status != "ok" {
                        		t.Errorf("expected ok, got %s", resp.Status)
                        	}
                        }
                    """.trimIndent()
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "go.mod",
                    language = "go",
                    content = """
                        module github.com/opencode/goservice

                        go 1.23.0

                        require github.com/gin-gonic/gin v1.10.0
                    """.trimIndent()
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "AGENTS.md",
                    language = "markdown",
                    content = "# OpenCode Go Service Rules\n- Handle error cases explicitly.\n- Run `go test ./...` before commit."
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "README.md",
                    language = "markdown",
                    content = "# $name\n\n$description\n\nRun `go run main.go` to start the server."
                )
            )

            "flutter" -> listOf(
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "lib/main.dart",
                    language = "dart",
                    content = """
                        import 'package:flutter/material.dart';

                        void main() {
                          runApp(const OpenCodeFlutterApp());
                        }

                        class OpenCodeFlutterApp extends StatelessWidget {
                          const OpenCodeFlutterApp({super.key});

                          @override
                          Widget build(BuildContext context) {
                            return MaterialApp(
                              title: '$name',
                              theme: ThemeData.dark(useMaterial3: true),
                              home: const HomeScreen(),
                            );
                          }
                        }

                        class HomeScreen extends StatefulWidget {
                          const HomeScreen({super.key});

                          @override
                          State<HomeScreen> createState() => _HomeScreenState();
                        }

                        class _HomeScreenState extends State<HomeScreen> {
                          int _counter = 0;

                          @override
                          Widget build(BuildContext context) {
                            return Scaffold(
                              appBar: AppBar(
                                title: const Text('$name'),
                                centerTitle: true,
                              ),
                              body: Center(
                                child: Column(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    const Text('Autonomous Agent Runs:', style: TextStyle(fontSize: 16)),
                                    Text('${'$'}_counter', style: Theme.of(context).textTheme.headlineMedium),
                                  ],
                                ),
                              ),
                              floatingActionButton: FloatingActionButton(
                                onPressed: () => setState(() => _counter++),
                                child: const Icon(Icons.bolt),
                              ),
                            );
                          }
                        }
                    """.trimIndent()
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "pubspec.yaml",
                    language = "yaml",
                    content = """
                        name: opencode_flutter_app
                        description: $description
                        version: 1.0.0+1
                        environment:
                          sdk: '>=3.2.0 <4.0.0'
                        dependencies:
                          flutter:
                            sdk: flutter
                          cupertino_icons: ^1.0.8
                        dev_dependencies:
                          flutter_test:
                            sdk: flutter
                    """.trimIndent()
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "AGENTS.md",
                    language = "markdown",
                    content = "# Flutter Agent Instructions\n- Use const constructors.\n- Structure UI into modular widgets in `lib/widgets`."
                )
            )

            "reactnative" -> listOf(
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "App.tsx",
                    language = "typescript",
                    content = """
                        import React, { useState } from 'react';
                        import { StyleSheet, Text, View, TouchableOpacity, SafeAreaView } from 'react-native';

                        export default function App() {
                          const [runs, setRuns] = useState(0);

                          return (
                            <SafeAreaView style={styles.container}>
                              <View style={styles.header}>
                                <Text style={styles.title}>$name</Text>
                                <Text style={styles.subtitle}>$description</Text>
                              </View>
                              <View style={styles.card}>
                                <Text style={styles.cardText}>Agent Executions: {runs}</Text>
                                <TouchableOpacity 
                                  style={styles.button}
                                  onPress={() => setRuns(r => r + 1)}
                                >
                                  <Text style={styles.buttonText}>Run AI Task</Text>
                                </TouchableOpacity>
                              </View>
                            </SafeAreaView>
                          );
                        }

                        const styles = StyleSheet.create({
                          container: { flex: 1, backgroundColor: '#0B0F19', padding: 20 },
                          header: { marginTop: 40, marginBottom: 20 },
                          title: { fontSize: 24, fontWeight: 'bold', color: '#38BDF8' },
                          subtitle: { fontSize: 14, color: '#94A3B8', marginTop: 4 },
                          card: { backgroundColor: '#1E293B', padding: 20, borderRadius: 12 },
                          cardText: { color: '#F1F5F9', fontSize: 16, marginBottom: 12 },
                          button: { backgroundColor: '#38BDF8', padding: 12, borderRadius: 8, alignItems: 'center' },
                          buttonText: { color: '#000', fontWeight: 'bold' }
                        });
                    """.trimIndent()
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "package.json",
                    language = "json",
                    content = """
                        {
                          "name": "opencode-react-native",
                          "version": "1.0.0",
                          "main": "node_modules/expo/AppEntry.js",
                          "scripts": {
                            "start": "expo start",
                            "android": "expo start --android",
                            "ios": "expo start --ios"
                          },
                          "dependencies": {
                            "expo": "~52.0.0",
                            "react": "18.3.1",
                            "react-native": "0.76.2"
                          }
                        }
                    """.trimIndent()
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "AGENTS.md",
                    language = "markdown",
                    content = "# React Native Expo Guidelines\n- Clean TypeScript styling\n- Support dark theme palettes"
                )
            )

            "springboot" -> listOf(
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "src/main/kotlin/com/example/demo/Application.kt",
                    language = "kotlin",
                    content = """
                        package com.example.demo

                        import org.springframework.boot.autoconfigure.SpringBootApplication
                        import org.springframework.boot.runApplication
                        import org.springframework.web.bind.annotation.*

                        @SpringBootApplication
                        class Application

                        fun main(args: Array<String>) {
                            runApplication<Application>(*args)
                        }

                        @RestController
                        @RequestMapping("/api/v1/agent")
                        class AgentController {

                            @GetMapping("/health")
                            fun health(): Map<String, String> = mapOf(
                                "service" to "$name",
                                "status" to "UP"
                            )

                            @PostMapping("/task")
                            fun executeTask(@RequestBody payload: Map<String, Any>): Map<String, Any> {
                                return mapOf(
                                    "status" to "COMPLETED",
                                    "output" to "Task executed by Spring Boot Cloud Core"
                                )
                            }
                        }
                    """.trimIndent()
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "build.gradle.kts",
                    language = "kotlin",
                    content = """
                        plugins {
                            id("org.springframework.boot") version "3.4.1"
                            id("io.spring.dependency-management") version "1.1.7"
                            kotlin("jvm") version "2.0.21"
                            kotlin("plugin.spring") version "2.0.21"
                        }

                        dependencies {
                            implementation("org.springframework.boot:spring-boot-starter-web")
                            implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
                            testImplementation("org.springframework.boot:spring-boot-starter-test")
                        }
                    """.trimIndent()
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "AGENTS.md",
                    language = "markdown",
                    content = "# Spring Boot Project Rules\n- Use Kotlin Idiomatic syntax.\n- Write JUnit 5 unit tests for endpoints."
                )
            )

            "rust" -> listOf(
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "src/main.rs",
                    language = "rust",
                    content = """
                        use clap::Parser;

                        #[derive(Parser, Debug)]
                        #[command(name = "$name", about = "$description")]
                        struct Cli {
                            #[arg(short, long, default_value = "plan")]
                            mode: String,
                            #[arg(short, long)]
                            query: Option<String>,
                        }

                        fn main() {
                            let args = Cli::parse();
                            println!("🚀 OpenCode Rust Engine running in mode: {}", args.mode);
                            if let Some(q) = args.query {
                                println!("Executing query: {}", q);
                            }
                        }
                    """.trimIndent()
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "Cargo.toml",
                    language = "rust",
                    content = """
                        [package]
                        name = "opencode_rust_cli"
                        version = "0.1.0"
                        edition = "2021"

                        [dependencies]
                        clap = { version = "4.5", features = ["derive"] }
                        serde = { version = "1.0", features = ["derive"] }
                        serde_json = "1.0"
                    """.trimIndent()
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "AGENTS.md",
                    language = "markdown",
                    content = "# Rust Core Engine Rules\n- Zero unwrap() in production code.\n- Run `cargo check` & `cargo test`."
                )
            )

            "compose" -> listOf(
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "app/src/main/java/com/example/MainActivity.kt",
                    language = "kotlin",
                    content = """
                        package com.example

                        import android.os.Bundle
                        import androidx.activity.ComponentActivity
                        import androidx.activity.compose.setContent
                        import androidx.compose.foundation.layout.*
                        import androidx.compose.material3.*
                        import androidx.compose.runtime.*
                        import androidx.compose.ui.Modifier

                        class MainActivity : ComponentActivity() {
                            override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)
                                setContent {
                                    MaterialTheme {
                                        Surface(modifier = Modifier.fillMaxSize()) {
                                            Text(text = "Welcome to $name")
                                        }
                                    }
                                }
                            }
                        }
                    """.trimIndent()
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "AGENTS.md",
                    language = "markdown",
                    content = "# Android Compose Guidelines\n- Material 3 components\n- ViewModel with StateFlow"
                )
            )

            else -> listOf(
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "src/index.ts",
                    language = "typescript",
                    content = "// OpenCode Project: $name\nexport function init() {\n  console.log('Initialized $name');\n}"
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "package.json",
                    language = "json",
                    content = "{\n  \"name\": \"${name.lowercase().replace(" ", "-")}\",\n  \"version\": \"1.0.0\"\n}"
                ),
                WorkspaceFileEntity(
                    projectId = projectId,
                    filePath = "AGENTS.md",
                    language = "markdown",
                    content = "# $name Rules\n- Write unit tests for all features."
                )
            )
        }
    }
}
