from flask import Flask, render_template, redirect, url_for, request, session, flash, g
from functools import wraps
import sqlite3
import time
from database import participantPairs, findCompleted, update
from functions import users, grabSourceCode
from functions import ClonePair

app = Flask(__name__)

app.secret_key = "random"
app.database = "clones_db.db"
app.users = users()


# login required decorator
def login_required(f):
    @wraps(f)
    def wrap(*args, **kwargs):
        if 'logged_in' in session:
            return f(*args, **kwargs)
        else:
            # flash('You need to login first.')
            return redirect(url_for('login'))
    return wrap

current_cp_no = 1
participant_pairs = []
start_time = time.time()
# flash message when nothing is selected
@app.route('/', methods=['GET','POST'])
@login_required
def home():

    global current_cp_no # 1 indexing
    global participant_pairs # contains the group information
    global start_time

    if ('Prev' in request.form):
        participant_pairs[current_cp_no-1][4] += (time.time() - start_time)
        start_time = time.time()
        if (current_cp_no == 1):
            flash('You have reached the first pair!')
        else:
            current_cp_no -= 1
        return redirect(url_for("home"))

    elif ('Next' in request.form):
        participant_pairs[current_cp_no-1][4] += (time.time() - start_time)
        start_time = time.time()
        if ('Clone_result' in request.form):
            result = resultParticipant(request.form['Clone_result'])
            participant_pairs[current_cp_no-1][3] = result
            participant_pairs[current_cp_no-1][5] = request.form['reason']

        if ('Clone_result' not in request.form and participant_pairs[current_cp_no-1][3] == None):
            flash('Please make the choice first!')

        elif (current_cp_no == 150):
            flash('You have reached the last pair. Kindly logout to save your answers!')
        else:
            current_cp_no += 1

        return redirect(url_for("home"))

    
    source_code1, source_code2, file1, file2 = grabSourceCode(session['userid'] ,current_cp_no)
    for i in range (len(participant_pairs)):
        p = participant_pairs[i]
        print(p[0], p[3], p[4], p[5])
        if (i == 10):
            break

    clonepair = ClonePair(current_cp_no, source_code1, source_code2, file1, file2, selectedResult(participant_pairs[current_cp_no-1][3]), participant_pairs[current_cp_no-1][4], participant_pairs[current_cp_no-1][5])
    return render_template("index.html", Clonepair = clonepair)


@app.route('/login', methods=['GET','POST'])
def login():
    error = None
    global participant_pairs
    global current_cp_no
    global start_time 
    start_time = time.time()

    if (request.method == 'POST'):
        if ( check_user(request.form['userid']) == False):
            error = 'Invalid userID. Please try again.'
        else:
            session['logged_in'] = True
            session['userid'] = request.form['userid']
            participant_pairs = participantPairs(session['userid'])
            current_cp_no = findCompleted(participant_pairs) + 1
            if (current_cp_no == 150):
                return render_template("logout.html")
            return redirect(url_for('home'))

    return render_template('login.html', error=error)

def check_user(user_id):
    return (user_id in app.users.keys())


@app.route('/logout')
@login_required
def logout():
    global participant_pairs
    update(participant_pairs, session['userid'])
    session.pop('logged_in', None)
    return render_template("logout.html")


def resultParticipant(value):
    if (value == 'clone'):
        return 1
    elif (value == 'not_clone'):
        return 0
    elif (value == 'unknown'):
        return 2

def selectedResult(value):
    if (value == 1):
        return 'Yes'
    elif (value == 0):
        return 'No'
    elif (value == 2):
        return "Can't say"

def connect_db():
    return sqlite3.connect(app.database)

if (__name__ == '__main__'):
    app.run(debug=False)


