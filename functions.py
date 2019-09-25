
def users():
    user_data = {}
    start_index = 1
    groups = 14
    clones_per_user = 82
    for i in range(1,groups+1):
        user_name1 = 'user'+str(i)+'_1'
        user_name2 = 'user'+str(i)+'_2'
        user_data[user_name1] = start_index
        user_data[user_name2] = start_index
        start_index += clones_per_user
    return (user_data)

def functionality():
    name = {}
    name[2] = "Download From Web"
    name[3] = "Secure Hash"
    name[4] = "Copy file"
    name[5] = "Decompress zip archive"
    name[6] = "Connect to FTP Server"
    name[7] = "Bubble Sort Array"
    name[8] = "Setup SVG"
    name[9] = "Setup SVG Event Handler"
    name[10] = "Execute update and rollback"
    name[11] = "Initialize Java Eclipse Project"
    name[12] = "Get prime factors"
    name[13] = "Shuffle Array in Place"
    name[14] = "Binary Search"
    name[15] = "Load Custom Fonts"
    name[17] = "Create Encryption Key Files"
    name[18] = "Play Sound"
    name[19] = "Take Screenshot to File"
    name[20] = "Fibonacci"
    name[21] = "XMPP Send Message"
    name[22] = "Encrypt To File"
    name[23] = "Resize Array"
    name[24] = "Open URL in System Browser"
    name[25] = "Open File in Desktop Application"
    name[26] = "GCD"
    name[27] = "Call Method using Reflection"
    name[28] = "Parse XML to DOM"
    name[29] = "Convert Date String Format"
    name[30] = "Zip Files"
    name[31] = "File Dialog"
    name[32] = "Send E-Mail"
    name[33] = "CRC32 File Checksum"
    name[34] = "Execute External Process"
    name[35] = "Instantiate Using Reflection"
    name[36] = "Connect to Database"
    name[37] = "Load File into Byte Array"
    name[38] = "Get MAC Address String"
    name[39] = "Delete Folder and Contents"
    name[40] = "Parse CSV Files"
    name[41] = "Transpose a Matrix"
    name[42] = "Extract Matches Using Regex"
    name[43] = "Copy Directory"
    name[44] = "Test Palindrome"
    name[45] = "Write PDF File"
    return name

