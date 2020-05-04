class ClonePair:
    def __init__(self, pairno, source_code1, source_code2, file1, file2, result, t, reason):
        self.pairno = str(pairno)
        self.source_code1 = source_code1
        self.source_code2 = source_code2
        self.file1 = file1
        self.file2 = file2
        self.result = result
        self.time = t
        self.reason = reason


def users():
    user_data = {}
    start_index = 1
    groups = 6
    clones_per_user = 150
    for i in range (1, groups+1):
        user_name1 = 'user'+str(i)+'_1'
        user_name2 = 'user'+str(i)+'_2'
        user_name3 = 'user'+str(i)+'_3'
        user_data[user_name1] = start_index
        user_data[user_name2] = start_index
        user_data[user_name3] = start_index
        start_index += clones_per_user
    return (user_data)

def grabSourceCode(userid, current_cp_no):
    user = users()
    cum_cp_no = user[userid] + current_cp_no - 1
    with open('pairs.txt', 'r') as f:
        pairs = f.read().split('\n')

    pair = pairs[cum_cp_no-1]
    pair = pair.split('\t')
    file1 = pair[0]
    file2 = pair[1]

    with open(file1, 'r', errors = 'ignore') as f:
        source_code1 = f.read()
    with open(file2, 'r', errors = 'ignore') as f:
        source_code2 = f.read()
    
    return source_code1, source_code2, file1, file2