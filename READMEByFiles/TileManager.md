 TileManager.java - Isometric Map System
🎯 What It Does
TileManager is the isometric floor renderer - it draws the ground/floor tiles of your game world in that distinctive diamond-shaped isometric view (like Habbo Hotel).
Think of it as the stage/floor on which everything else sits (players, furniture, etc.).

🏗️ Core Responsibilities
TileManager
├── 📦 Load tile images (grass, wood floor, etc.)
├── 📄 Load map layout from text file
├── 🎨 Render isometric grid
└── 📍 Manage camera offset (panning/centering)