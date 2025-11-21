# Contributing to A2A Gateway

Thank you for your interest in contributing! This document provides guidelines for contributing to the project.

## 🚀 Getting Started

1. **Fork the repository**
2. **Clone your fork**
   ```bash
   git clone https://github.com/your-username/agent-nexus.git
   cd agent-nexus
   ```
3. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

## 🛠️ Development Setup

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- OpenAI API key

### Local Development
```bash
# Start PostgreSQL
docker run -d --name postgres \
  -e POSTGRES_USER=quarkus \
  -e POSTGRES_PASSWORD=quarkus \
  -e POSTGRES_DB=agent_nexus \
  -p 5432:5432 postgres:17

# Set environment variables
export OPENAI_API_KEY=your-key-here

# Run in dev mode
./mvnw quarkus:dev
```

## 📝 Code Guidelines

### Java Code Style
- Follow standard Java conventions
- Use meaningful variable names
- Add JavaDoc for public methods
- Keep methods focused and small

### REST API Guidelines
- Use RESTful conventions
- Proper HTTP status codes
- Consistent JSON response format
- Document all endpoints

### Frontend Code
- Use vanilla JavaScript (no build step)
- Keep functions pure when possible
- Comment complex D3.js logic
- Maintain accessibility

## 🧪 Testing

### Run Tests
```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw verify

# Manual testing
./test-multi-agent.sh
```

### Writing Tests
- Add unit tests for new services
- Test edge cases and error conditions
- Use descriptive test names

## 📦 Pull Request Process

1. **Update documentation** if needed
2. **Add tests** for new features
3. **Ensure tests pass**
   ```bash
   ./mvnw clean verify
   ```
4. **Update CHANGELOG.md** (if exists)
5. **Create pull request** with description:
   - What changes were made
   - Why they were needed
   - How to test them

### PR Title Format
```
[Feature|Fix|Docs|Refactor]: Brief description

Example:
Feature: Add conversation history support
Fix: Resolve agent delegation cycle detection
Docs: Update API documentation
```

## 🐛 Bug Reports

### Before Submitting
- Check existing issues
- Try to reproduce with latest version
- Gather relevant logs/screenshots

### Bug Report Template
```markdown
**Description**
Clear description of the bug

**Steps to Reproduce**
1. Step one
2. Step two
3. ...

**Expected Behavior**
What should happen

**Actual Behavior**
What actually happens

**Environment**
- OS: 
- Java version:
- Docker version:

**Logs**
```
paste relevant logs
```
```

## 💡 Feature Requests

### Feature Request Template
```markdown
**Problem**
What problem does this solve?

**Proposed Solution**
How would you implement it?

**Alternatives Considered**
Other approaches?

**Additional Context**
Screenshots, examples, etc.
```

## 🎯 Priority Areas for Contribution

### High Priority
- [ ] WebSocket support for real-time UI updates
- [ ] Conversation history persistence
- [ ] Agent health monitoring
- [ ] Cycle detection in agent delegation
- [ ] Rate limiting

### Medium Priority
- [ ] PUT endpoint for agent updates
- [ ] Agent templates/presets
- [ ] Metrics and monitoring
- [ ] Multi-LLM provider support
- [ ] Agent conversation export

### Documentation
- [ ] Video walkthrough
- [ ] Architecture diagrams
- [ ] API examples in multiple languages
- [ ] Deployment guides (Kubernetes, AWS, Azure)

## 🔒 Security

### Reporting Security Issues
**Do not open public issues for security vulnerabilities.**

Email: danieltse@gmail.com

### Security Checklist
- [ ] No sensitive data in logs
- [ ] Input validation
- [ ] SQL injection prevention
- [ ] XSS prevention in UI
- [ ] Dependency scanning

## 📚 Resources

- [Quarkus Documentation](https://quarkus.io/guides/)
- [LangChain4j Documentation](https://github.com/langchain4j/langchain4j)
- [D3.js Documentation](https://d3js.org/)
- [OpenAI API Reference](https://platform.openai.com/docs/)

## 💬 Community

- **Issues**: GitHub Issues for bugs/features
- **Discussions**: GitHub Discussions for questions
- **Email**: [your-email@example.com]

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing! 🎉
