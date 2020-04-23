import sqlite3
import os
from functions import users


def participantPairs(userid):
    connection = sqlite3.connect("clones_db.db")
    c = connection.cursor()
    users_dic = users()
    start_index = users_dic[userid]
    end_index = start_index + 150
    query = "select * from CLONETABLE where Id >= "+str(start_index)+" and Id < " + str(end_index)
    cur = c.execute(query)
    tuples = cur.fetchall()
    participant_no = int(userid[6])
    column = 2 + participant_no
    pairs = []
    for t in tuples:
        time = 0
        if (t[column+3] != None):
            time = float(t[column+3])
        pairs.append([t[0], t[1], t[2], t[column], time, t[column+6]]) 
    return pairs

def findCompleted(participant_pairs):
    completed = 0
    for p in participant_pairs:
        if (p[3] == None):
            return completed
        completed += 1
    return completed


def update(participant_pairs, userid):
    connection = sqlite3.connect("clones_db.db")
    c = connection.cursor()
    participant = userid[6]
    users_dic = users()
    start_index = users_dic[userid]
    end_index = start_index + 150
    p = 0
    for i in range(start_index, end_index):
        if (participant_pairs[p][3] == None):
            break
        query = 'UPDATE CLONETABLE SET ' 
        query += 'Participant_'+participant+'= '+str(participant_pairs[p][3]) +', '
        query += 'Time_'+participant+'= "'+str(participant_pairs[p][4]) + '", '
        query += 'Message_'+participant+'= "'+participant_pairs[p][5] + '" '
        query += 'WHERE Id = '+ str(i)
        c.execute(query)
        connection.commit()
        p+=1


# connection = sqlite3.connect("clones_db.db")
# c = connection.cursor()
# with open('pairs.txt', 'r') as f:
#     pairs = f.read().split('\n')
# i = 1
# for pair in pairs:
#     pair = pair.split('\t')
#     file1 = pair[0]
#     file2 = pair[1]
#     query = 'UPDATE CLONETABLE SET ' 
#     query += 'File_1 = "' +file1 +'", '
#     query += 'File_2 = "' +file2 +'" '
#     query += 'WHERE Id = '+ str(i)
#     i += 1
#     print(query)
#     c.execute(query)
#     connection.commit()