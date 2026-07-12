#!/bin/bash

TOKEN=$1

# Check if token is provided
if [ -z "$TOKEN" ]; then
  echo "Error: Please provide a GitHub token."
  echo "Usage: bash push_to_github.sh ghp_yourTokenHere"
  exit 1
fi

# Get GitHub Username securely from the API
USERNAME=$(curl -s -H "Authorization: token $TOKEN" https://api.github.com/user | grep -o '"login": "[^"]*"' | head -n 1 | cut -d'"' -f4)

if [ -z "$USERNAME" ]; then
  echo "Error: Invalid token. Could not log into GitHub."
  exit 1
fi

echo "✓ Logged in as: $USERNAME"

# Create the repository on GitHub
echo "Creating repository 'bar-prep' on GitHub..."
curl -s -H "Authorization: token $TOKEN" -d '{"name":"bar-prep", "private":false}' https://api.github.com/user/repos > /dev/null

echo "✓ Repository created: https://github.com/$USERNAME/bar-prep"

# Initialize Git, add files, and commit
git init
git branch -M main
git add .
git commit -m "Initial commit of BarPrepApp"

# Setup remote and push
git remote remove origin 2>/dev/null
git remote add origin https://$TOKEN@github.com/$USERNAME/bar-prep.git

echo "Pushing code to GitHub..."
git push -u origin main

echo "✅ Pushed to GitHub"
echo "  Repo:    https://github.com/$USERNAME/bar-prep"
echo "  Actions: https://github.com/$USERNAME/bar-prep/actions"