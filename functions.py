
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
