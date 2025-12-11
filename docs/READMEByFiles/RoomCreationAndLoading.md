You're using a custom pipe-delimited format.
Your Current Flow
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. CREATE ROOM (in memory)                                                  │
│    Room room = new Room("caca", "TRUMP", 9, 5);                             │
│    → Java object with fields: roomId, roomName, ownerUsername, etc.        │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ 2. SERIALIZE (save to file)                                                 │
│    room.serialize() returns:                                                │
│    "room_1765334209845_102|caca|TRUMP|PUBLIC||25|9|5|1,1,1,1,..."          │
│                            ↑                                                │
│                      Pipe-separated string (NOT JSON!)                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ 3. WRITE TO FILE (rooms.dat)                                                │
│    writer.write(room.serialize());                                          │
│    writer.newLine();                                                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ 4. DESERIALIZE (load from file)                                             │
│    Room.deserialize("room_1765334209845_102|caca|TRUMP|PUBLIC||25|9|5|...") │
│    → Splits by "|", creates new Room object                                 │
└─────────────────────────────────────────────────────────────────────────────┘