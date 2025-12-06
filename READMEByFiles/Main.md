# 🚀 Main.java - Application Entry Point

> **The entry point that launches Futura via custom URL protocol**

---

## 🎯 **PURPOSE**

`Main` is the **entry point** of the Futura virtual world application. It:
- Receives launch parameters via custom URL protocol (`futura://`)
- Parses username and gender from URL
- Creates the game window with user credentials
- Enables direct launch from web browsers or other applications

**Think of it as**: The front door that lets users join the virtual world directly from a link!

---

## 🌐 **CUSTOM URL PROTOCOL**

### **Format**
```
futura://open?user=USERNAME&gender=GENDER
```

### **Examples**
```bash
# Launch as Joe (male)
futura://open?user=Joe&gender=male

# Launch as Alice (female)
futura://open?user=Alice&gender=female

# Launch as JoeRogan (male)
futura://open?user=JoeRogan&gender=male
```

### **Use Cases**
- 🌐 **Website links** - Click to join
- 📧 **Email invitations** - Direct access
- 💬 **Chat messages** - Share game links
- 📱 **Mobile apps** - Deep linking
- 🖥️ **Desktop shortcuts** - Quick launch

---

## 📊 **HOW IT WORKS**

```
User clicks: futura://open?user=Joe&gender=male
        ↓
Operating System launches app with this URL as argument
        ↓
Main.main(args) receives: args[0] = "futura://open?user=Joe&gender=male"
        ↓
parseArguments() extracts: user=Joe, gender=male
        ↓
Creates LaunchConfig(username="Joe", gender="male")
        ↓
SwingUtilities.invokeLater() ensures UI is on correct thread
        ↓
new GameWindow("Joe", "male")
        ↓
Game launches with player named Joe (male character)
```

---

## 🔧 **CODE BREAKDOWN**

### **1. Constants**
```java
private static final String DEFAULT_USERNAME = "Dominique";
private static final String DEFAULT_GENDER = "male";
private static final String PROTOCOL = "futura://";
```

**Purpose**:
- `DEFAULT_USERNAME` - Used if no URL provided (testing)
- `DEFAULT_GENDER` - Used if no gender specified
- `PROTOCOL` - The custom URL scheme identifier

---

### **2. main() Method**
```java
public static void main(String[] args) {
    // Parse launch arguments from URL scheme
    LaunchConfig config = parseArguments(args);
    
    // Debug output
    System.out.println("Username: " + config.username);
    System.out.println("Gender: " + config.gender);
    
    // Launch game on Swing thread
    SwingUtilities.invokeLater(() -> {
        new GameWindow(config.username, config.gender);
    });
}
```

**What it does**:
1. **Parses arguments** - Extracts username/gender from URL
2. **Prints debug info** - Shows what was parsed
3. **Launches game** - Creates GameWindow on Swing thread

**Why `SwingUtilities.invokeLater()`?**
- Swing UI must run on the **Event Dispatch Thread (EDT)**
- This ensures thread safety
- Prevents UI freezes and crashes

---

### **3. parseArguments() Method**
```java
private static LaunchConfig parseArguments(String[] args) {
    String username = DEFAULT_USERNAME;
    String gender = DEFAULT_GENDER;
    
    if (args.length > 0 && args[0].startsWith(PROTOCOL)) {
        // Extract query string after '?'
        int queryStart = args[0].indexOf("?");
        if (queryStart != -1) {
            String query = args[0].substring(queryStart + 1);
            String[] pairs = query.split("&");
            
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    String key = kv[0].toLowerCase();
                    String value = kv[1];
                    
                    if (key.equals("user")) {
                        username = value;
                    } else if (key.equals("gender")) {
                        gender = value;
                    }
                }
            }
        }
    }
    
    return new LaunchConfig(username, gender);
}
```

**Parsing Steps**:

```
Input: "futura://open?user=Joe&gender=male"
  ↓
1. Check if starts with "futura://"
  ↓
2. Find '?' → queryStart = 19
  ↓
3. Extract query: "user=Joe&gender=male"
  ↓
4. Split by '&': ["user=Joe", "gender=male"]
  ↓
5. For each pair:
   - Split by '=': ["user", "Joe"]
   - If key == "user": username = "Joe"
   - If key == "gender": gender = "male"
  ↓
6. Return LaunchConfig("Joe", "male")
```

---

### **4. LaunchConfig Class**
```java
private static class LaunchConfig {
    final String username;
    final String gender;
    
    LaunchConfig(String username, String gender) {
        this.username = username;
        this.gender = gender;
    }
}
```

**Purpose**: Simple data container (no logic, just data)

**Why not use separate variables?**
- ✅ Cleaner - Pass one object instead of two variables
- ✅ Type-safe - Can't mix up username/gender order
- ✅ Extensible - Easy to add more fields later

**Example**:
```java
// ❌ BAD: Easy to mix up order
launchGame(gender, username);  // Oops! Wrong order!

// ✅ GOOD: Type-safe
LaunchConfig config = new LaunchConfig(username, gender);
launchGame(config);  // Can't mess this up!
```

---

## 🎮 **USAGE EXAMPLES**

### **Example 1: Direct Launch (No URL)**
```bash
# Run from terminal
java -cp bin main.Main

# Result:
Username: Dominique
Gender: male
# Launches with default values
```

---

### **Example 2: URL Launch (Windows)**
```bash
# Run from terminal
java -cp bin main.Main "futura://open?user=Joe&gender=male"

# Result:
Username: Joe
Gender: male
# Launches with Joe (male)
```

---

### **Example 3: URL Launch (Web Browser)**
```html
<!-- HTML link -->
<a href="futura://open?user=Alice&gender=female">
    Join as Alice
</a>

<!-- User clicks link -->
<!-- Browser asks: "Open Futura?" -->
<!-- User clicks "Yes" -->
<!-- App launches with Alice (female) -->
```

---

### **Example 4: Email Invitation**
```
Hey! Join me in Futura!

Click here: futura://open?user=Bob&gender=male

See you in the virtual world! 🎮
```

---

### **Example 5: Programmatic Launch (Java)**
```java
// Another app can launch Futura
String username = "Charlie";
String gender = "male";
String url = "futura://open?user=" + username + "&gender=" + gender;

Runtime.getRuntime().exec(new String[] {
    "java", "-cp", "bin", "main.Main", url
});
```

---

## 🔄 **LAUNCH FLOW DIAGRAM**

```
┌─────────────────────────────────────────────┐
│  User clicks link or runs command           │
└──────────────────┬──────────────────────────┘
                   ↓
┌─────────────────────────────────────────────┐
│  Operating System receives:                 │
│  "futura://open?user=Joe&gender=male"       │
└──────────────────┬──────────────────────────┘
                   ↓
┌─────────────────────────────────────────────┐
│  OS launches: java main.Main [URL]          │
└──────────────────┬──────────────────────────┘
                   ↓
┌─────────────────────────────────────────────┐
│  main() called with args[0] = URL           │
└──────────────────┬──────────────────────────┘
                   ↓
┌─────────────────────────────────────────────┐
│  parseArguments() extracts:                 │
│  - username = "Joe"                         │
│  - gender = "male"                          │
└──────────────────┬──────────────────────────┘
                   ↓
┌─────────────────────────────────────────────┐
│  Create LaunchConfig("Joe", "male")         │
└──────────────────┬──────────────────────────┘
                   ↓
┌─────────────────────────────────────────────┐
│  SwingUtilities.invokeLater()               │
│  (ensures UI thread safety)                 │
└──────────────────┬──────────────────────────┘
                   ↓
┌─────────────────────────────────────────────┐
│  new GameWindow("Joe", "male")              │
└──────────────────┬──────────────────────────┘
                   ↓
┌─────────────────────────────────────────────┐
│  Game launches!                             │
│  Player "Joe" enters virtual world          │
└─────────────────────────────────────────────┘
```

---

## ⚙️ **SETUP: REGISTER URL PROTOCOL**

To make `futura://` work, you need to register it with the OS:

### **Windows (Registry)**
```reg
Windows Registry Editor Version 5.00

[HKEY_CLASSES_ROOT\futura]
@="URL:Futura Protocol"
"URL Protocol"=""

[HKEY_CLASSES_ROOT\futura\shell]

[HKEY_CLASSES_ROOT\futura\shell\open]

[HKEY_CLASSES_ROOT\futura\shell\open\command]
@="\"C:\\Path\\To\\java.exe\" -cp \"C:\\Path\\To\\bin\" main.Main \"%1\""
```

### **macOS (Info.plist)**
```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLName</key>
        <string>Futura URL</string>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>futura</string>
        </array>
    </dict>
</array>
```

### **Linux (Desktop Entry)**
```ini
[Desktop Entry]
Name=Futura
Exec=java -cp /path/to/bin main.Main %u
Type=Application
MimeType=x-scheme-handler/futura
```

---

## 🧪 **TESTING**

### **Test 1: No Arguments**
```bash
java -cp bin main.Main

Expected output:
Username: Dominique
Gender: male
```

### **Test 2: URL with User**
```bash
java -cp bin main.Main "futura://open?user=TestUser&gender=female"

Expected output:
Username: TestUser
Gender: female
```

### **Test 3: Invalid URL**
```bash
java -cp bin main.Main "http://example.com"

Expected output:
Username: Dominique
Gender: male
# Falls back to defaults (not a futura:// URL)
```

### **Test 4: Missing Gender**
```bash
java -cp bin main.Main "futura://open?user=OnlyUser"

Expected output:
Username: OnlyUser
Gender: male
# Uses default gender
```

---

## 🔍 **ERROR HANDLING**

### **Case 1: Empty Arguments**
```java
args = []
Result: Uses DEFAULT_USERNAME and DEFAULT_GENDER
```

### **Case 2: Invalid URL Format**
```java
args[0] = "not-a-futura-url"
Result: Uses DEFAULT_USERNAME and DEFAULT_GENDER
```

### **Case 3: Malformed Query**
```java
args[0] = "futura://open?noequals"
Result: Ignores invalid pairs, uses defaults
```

### **Case 4: Special Characters**
```java
args[0] = "futura://open?user=Joe%20Smith&gender=male"
Note: URL encoding (%20 = space) is NOT decoded
Result: Username will be "Joe%20Smith" literally
```

**Improvement needed?** Add URL decoding:
```java
username = URLDecoder.decode(value, StandardCharsets.UTF_8);
```

---

## 💡 **DESIGN DECISIONS**

### **Why Custom URL Protocol?**
✅ **Direct Access** - Users join with one click
✅ **No Login Screen** - Credentials in URL
✅ **Web Integration** - Launch from websites
✅ **Cross-Platform** - Works on Windows/Mac/Linux

### **Why LaunchConfig Class?**
✅ **Type Safety** - Can't mix up parameters
✅ **Extensibility** - Easy to add more fields
✅ **Clarity** - Clear data structure

### **Why SwingUtilities.invokeLater()?**
✅ **Thread Safety** - UI on correct thread
✅ **Best Practice** - Swing requirement
✅ **Prevents Deadlocks** - Avoids UI freezes

---

## 🎯 **COMPARISON: WITH vs WITHOUT URL PROTOCOL**

### **Without URL Protocol (Traditional)**
```
1. User opens app manually
2. App shows login screen
3. User types username
4. User selects gender
5. User clicks "Join"
6. Game launches
```
**Total steps**: 6

### **With URL Protocol (Futura)**
```
1. User clicks link
2. Game launches
```
**Total steps**: 2 ✨

---

## 📊 **DATA FLOW**

```
URL String
    ↓
main(String[] args)
    ↓
parseArguments(args)
    ↓
LaunchConfig object
    ↓
SwingUtilities.invokeLater()
    ↓
GameWindow constructor
    ↓
GamePanel constructor
    ↓
Player object created with username/gender
    ↓
Game world initialized
```

---

## 🔗 **INTEGRATION EXAMPLES**

### **Website Integration**
```html
<!DOCTYPE html>
<html>
<head>
    <title>Join Futura</title>
</head>
<body>
    <h1>Welcome to Futura!</h1>
    
    <!-- Quick join buttons -->
    <button onclick="launch('Alice', 'female')">
        Join as Alice
    </button>
    
    <button onclick="launch('Bob', 'male')">
        Join as Bob
    </button>
    
    <script>
        function launch(user, gender) {
            window.location = `futura://open?user=${user}&gender=${gender}`;
        }
    </script>
</body>
</html>
```

---

### **Discord Bot Integration**
```python
# Discord bot that generates join links
@bot.command()
async def join(ctx, username: str, gender: str):
    link = f"futura://open?user={username}&gender={gender}"
    await ctx.send(f"Click to join: {link}")
```

---

### **QR Code Generation**
```python
import qrcode

# Generate QR code for joining as "Player1"
url = "futura://open?user=Player1&gender=male"
qr = qrcode.make(url)
qr.save("join_futura.png")

# Users scan QR code with phone -> App launches!
```

---

## 🛠️ **CUSTOMIZATION**

### **Change Default Values**
```java
private static final String DEFAULT_USERNAME = "Guest";
private static final String DEFAULT_GENDER = "male";
```

### **Change Protocol Name**
```java
private static final String PROTOCOL = "mygame://";
// Don't forget to register "mygame://" with OS!
```

### **Add More Parameters**
```java
private static class LaunchConfig {
    final String username;
    final String gender;
    final String room;      // NEW
    final int level;        // NEW
    
    LaunchConfig(String username, String gender, String room, int level) {
        this.username = username;
        this.gender = gender;
        this.room = room;
        this.level = level;
    }
}

// Parse "futura://open?user=Joe&gender=male&room=lobby&level=5"
```

---

## 📝 **SUMMARY**

**Main.java** is the **entry point** that:

1. **Receives** launch parameters via custom URL
2. **Parses** username and gender from URL
3. **Creates** LaunchConfig data object
4. **Launches** GameWindow on Swing thread
5. **Enables** direct access from web/email/etc

**Key Features**:
- ✅ Custom URL protocol (`futura://`)
- ✅ Simple URL parsing
- ✅ Default values (for testing)
- ✅ Thread-safe UI launch
- ✅ Clean code structure

**Benefits**:
- 🚀 One-click access
- 🌐 Web integration
- 📧 Email invitations
- 💬 Share game links
- 🎮 Instant join

---

## 🔗 **RELATED FILES**

- `GameWindow.java` - Creates the window (called by Main)
- `GamePanel.java` - The actual game (created by GameWindow)
- `Player.java` - Player character (uses username/gender from Main)

---

**That's Main.java!** 🚀

The front door to your virtual world! ✨