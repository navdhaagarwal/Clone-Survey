from flask import Flask, render_template, redirect, url_for, request, session, flash, g
from functools import wraps
import sqlite3
import time
from database import clone_pairs, java_content, update, update_time
from functions import users, functionality

app = Flask(__name__)

app.secret_key = "random"
app.database = "clones_db.db"
app.users = users()
app.functionality = functionality()

start_time = 0

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

# flash message when nothing is selected
@app.route('/', methods=['GET','POST'])
@login_required
def home():

    global start_time
    current_userid = session['userid']
    current_clone_no = session['current_clone_no']
    clone_pairs = session['clone_pairs']
    current_clone_pair = clone_pairs[current_clone_no-1]
    r = session['result'][current_clone_no-1]
    Clone_reason = session['reason'][current_clone_no-1]
    if(r == 0):
        result = "No"
    elif(r == 1):
        result = "Yes"
    elif(r == 2):
        result = "Can't say"
    else:
        result = "None"


    lines = [str(current_clone_pair[3]+1), str(current_clone_pair[4]+1), str(current_clone_pair[6]+1), str(current_clone_pair[7]+1)]
    contents, contents1 , info= java_content(current_clone_no, clone_pairs)

    if ('Prev' in request.form):
        if(current_clone_no > 1):

            current_time = time.time()
            session['time'][current_clone_no-1] += current_time - start_time
            start_time = time.time()

            current_clone_no -= 1
            session['current_clone_no'] = current_clone_no
            return redirect(url_for("home"))

    if (request.method == 'POST'):
        print(session['result'][current_clone_no-1])
        print(request.form)
        if( "Clone_result" not in request.form and session['result'][current_clone_no-1] == -1):
            flash('Kindly select an option')
            return redirect(url_for("home"))
        
        if("Clone_result" in request.form):
            Clone_res_value = request.form['Clone_result']
            if (Clone_res_value == 'clone'):
                session['result'][current_clone_no-1] = 1
            if (Clone_res_value == 'not_clone'):
                session['result'][current_clone_no-1] = 0
            if (Clone_res_value == 'unknown'):
                session['result'][current_clone_no-1] = 2

            Clone_reason = request.form['reason']
            session['reason'][current_clone_no-1] = Clone_reason

            update(current_userid, current_clone_no, app.users, session['result'][current_clone_no-1], Clone_reason)

        if ('Next' in request.form):
            if(current_clone_no < 82):

                current_time = time.time()
                session['time'][current_clone_no-1] += current_time - start_time
                start_time = time.time()

                current_clone_no += 1
                session['current_clone_no'] = current_clone_no
                return redirect(url_for("home"))

        print(current_clone_no)
        
    return render_template("index.html", contents = contents, contents1 = contents1, clone_pair = str(current_clone_no), lines = lines, info=info, functionality = app.functionality, result=result, reason=Clone_reason)

@app.route('/login', methods=['GET','POST'])
def login():
    error = None
    global start_time
    if (request.method == 'POST'):
        if ( check_user(request.form['userid']) == False):
            error = 'Invalid userID. Please try again.'
        else:
            session['logged_in'] = True
            start_time = time.time()
            if('first_login' not in session):
                session['userid'] = request.form['userid']
                session['current_clone_no'] = 1
                session['result'] = [-1]*82
                session['time'] = [0]*82
                session['reason'] = ['']*82
                session['first_login'] = True
                g.db = connect_db()
                session['clone_pairs'] = clone_pairs(g.db, app.users, request.form['userid'])
            return redirect(url_for('home'))

    return render_template('login.html',error=error)

def check_user(user_id):
    return (user_id in app.users)

@app.route('/logout')
@login_required
def logout():
    update_time( session['userid'] , session['time'], app.users)
    session.pop('logged_in', None)
    return render_template("logout.html")


def connect_db():
    return sqlite3.connect(app.database)

if (__name__ == '__main__'):
    app.run(debug=True)