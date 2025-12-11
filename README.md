
TO launch the game:

java -cp bin main.Main "futura://open?user=Joe&gender=male"
UPDATE -> java -cp "bin;lib/*" main.Main "futura://open?user=TRUMP&gender=male" (because include jar files to activate kafka)


To launch the server:

java -cp bin network.GameServer

# FOR COMPILING (powershell)

Option A - Simple (compile all at once):
powershelljavac -d bin -cp bin (Get-ChildItem -Recurse -Filter *.java src).FullName

Option B - Explicit (better for debugging):
powershelljavac -d bin -cp bin src\room\*.java src\Entity\*.java src\main\*.java src\message\*.java src\mouse\*.java src\network\*.java src\object\*.java src\pathfinding\*.java src\tile\*.java

Option C - Using a file list (most reliable):
powershell# Generate list of all .java files

Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName } | Out-File -Encoding ascii sources.txt
# Compile using the list
javac -d bin -cp bin @sources.txt





