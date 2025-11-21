# GitHub Setup Guide

Quick guide to get your A2A Gateway project on GitHub.

## Step 1: Create GitHub Repository

1. Go to [github.com](https://github.com) and sign in
2. Click the **+** icon → **New repository**
3. Fill in:
   - **Repository name**: `agent-nexus`
   - **Description**: "Central hub for multi-agent AI orchestration with real-time visualization"
   - **Visibility**: Public or Private
   - **DO NOT** initialize with README (we already have one)
4. Click **Create repository**

## Step 2: Initialize Git (if needed)

```bash
cd /Users/0xdant/dev/agent-nexus

# Check if git is already initialized
git status

# If not initialized, run:
git init
```

## Step 3: Add Files to Git

```bash
# Add all files (respects .gitignore)
git add .

# Check what will be committed
git status

# You should NOT see:
# - target/
# - *.log files
# - .env (but .env.example should be included)

# Commit
git commit -m "Initial commit: Agent Nexus - Multi-agent orchestration hub

- Quarkus + LangChain4j backend
- D3.js interactive visualization
- PostgreSQL persistence
- Docker Compose deployment
- Complete documentation"
```

## Step 4: Connect to GitHub

```bash
# Replace YOUR_USERNAME with your GitHub username
git remote add origin https://github.com/YOUR_USERNAME/agent-nexus.git

# Verify remote
git remote -v
```

## Step 5: Push to GitHub

```bash
# Push to main branch
git branch -M main
git push -u origin main
```

## Step 6: Verify on GitHub

1. Refresh your GitHub repository page
2. You should see:
   - ✅ README.md displayed
   - ✅ All source code
   - ✅ Documentation in `docs/` folder
   - ✅ Docker configuration files
   - ✅ LICENSE file

## Step 7: Add Topics/Tags

On your GitHub repository page:
1. Click **⚙️ Settings** (or find "About" section)
2. Add topics:
   - `ai`
   - `agent`
   - `multi-agent`
   - `quarkus`
   - `langchain4j`
   - `openai`
   - `gpt-4`
   - `d3js`
   - `visualization`
   - `docker`
   - `java`

## Step 8: Enable GitHub Pages (Optional)

For documentation hosting:
1. Go to **Settings** → **Pages**
2. Source: Deploy from branch
3. Branch: `main` → `/docs`
4. Save

## Step 9: Add Social Preview Image (Optional)

1. Take a screenshot of your UI
2. Go to **Settings** → **Options**
3. Scroll to "Social preview"
4. Upload screenshot (recommended: 1280x640px)

## Optional Enhancements

### Add GitHub Actions (CI/CD)

Create `.github/workflows/build.yml`:

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Build with Maven
        run: ./mvnw clean verify
```

### Add Issue Templates

Create `.github/ISSUE_TEMPLATE/bug_report.md`
Create `.github/ISSUE_TEMPLATE/feature_request.md`

### Add Pull Request Template

Create `.github/pull_request_template.md`

## Sharing Your Repository

### For Your Portfolio
Add to your GitHub profile README:

```markdown
## 🤖 Agent Nexus
Central hub for multi-agent AI orchestration with real-time visualization.
Built with Quarkus, LangChain4j, and D3.js.

[View Project](https://github.com/YOUR_USERNAME/agent-nexus)
```

### For LinkedIn
Post about your project:
```
🚀 Just built an AI Agent Orchestration Platform!

✨ Features:
- Dynamic agent creation with GPT-4o
- Multi-agent delegation & coordination
- Real-time D3.js visualization
- Full Docker containerization

🛠️ Tech: Java 21, Quarkus, LangChain4j, PostgreSQL, D3.js

Check it out: [link to your repo]

#AI #MultiAgent #Java #OpenSource
```

### For Twitter/X
```
Built an AI multi-agent system with real-time visualization! 🤖

🔄 Dynamic agent orchestration
📊 Interactive D3.js graphs
🐳 Docker-ready
⚡ Quarkus + GPT-4o

[link to repo]

#AI #MultiAgent #OpenSource
```

## Demo Video (Recommended)

1. Record a 2-3 minute demo using:
   - QuickTime (Mac)
   - OBS Studio
   - Loom
2. Upload to YouTube
3. Add link to README:
   ```markdown
   ## 📹 Demo Video
   
   [![Watch Demo](https://img.youtube.com/vi/YOUR_VIDEO_ID/0.jpg)](https://www.youtube.com/watch?v=YOUR_VIDEO_ID)
   ```

## Maintenance

### Regular Updates
```bash
# Make changes
git add .
git commit -m "Feature: Add conversation history"
git push
```

### Creating Releases
1. Go to **Releases** → **Create a new release**
2. Tag: `v1.0.0`
3. Title: "Initial Release"
4. Description: List features and changes
5. Publish release

## Need Help?

- [GitHub Docs](https://docs.github.com)
- [Git Tutorial](https://git-scm.com/docs/gittutorial)
- [Markdown Guide](https://guides.github.com/features/mastering-markdown/)

---

Good luck with your repository! 🎉
