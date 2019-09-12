import sqlite3
import os

def clone_pairs(connection,  users, current_userid):
    c = connection.cursor()
    start_index = users[current_userid]
    clones_per_user = 82
    print(start_index)
    query = "select * from CLONES where id >= "+str(start_index)+" and id < " + str(start_index+clones_per_user)
    cur = c.execute(query)
    return cur.fetchall()


def java_content(current_clone_no, clone_pairs):
    row = clone_pairs[current_clone_no-1]
    functionality_id = row[1]
    code_file_1 = row[2]
    code_file_2 = row[5]
    info = [code_file_1,code_file_2,functionality_id]
    print(code_file_1)
    dir_path = "C:/Users/navdh/Desktop/project/bcb_reduced/" + str(functionality_id)
    folders = ["/default","/selected","/sample"]
    contents = ""
    contents1 = ""
    for folder in folders:
        dir_path1 = dir_path + folder
        files = os.listdir(dir_path1)
        for f in files:
            if (f == code_file_1):
                fd = open(dir_path1+'/'+f,'r')
                print("yes1")
                contents = fd.read()
            if (f == code_file_2):
                fd = open(dir_path1+'/'+f,'r')
                print("yes2")
                contents1 = fd.read()
    return contents, contents1, info



def update(current_userid, current_clone_no, users, result):
    connection = sqlite3.connect("clones_db.db")
    c = connection.cursor()
    index = users[current_userid] + current_clone_no - 1
    length = len(current_userid)
    participant = current_userid[length-1]
    if(participant == '1'):
        p = 'ONE'
    else:
        p = 'TWO'
    query = 'UPDATE CLONES SET participant_'+p+ '= '+str(result)+' WHERE id = ' + str(index)
    c.execute(query)
    connection.commit()


