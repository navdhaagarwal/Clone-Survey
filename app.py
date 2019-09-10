from flask import Flask, render_template, redirect, url_for, request, session, flash, g
from functools import wraps
import sqlite3
from database import clone_pairs, java_content, update_true, update_false
from functions import users

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

# flash message when nothing is selected
@app.route('/', methods=['GET','POST'])
@login_required
def home():

    current_userid = session['userid']
    current_clone_no = session['current_clone_no']
    clone_pairs = session['clone_pairs']
    current_clone_pair = clone_pairs[current_clone_no-1]

    lines = [str(current_clone_pair[3]+1), str(current_clone_pair[4]+1), str(current_clone_pair[6]+1), str(current_clone_pair[7]+1)]
    contents, contents1 , info= java_content(current_clone_no, clone_pairs)

    if (request.method == 'POST'):
        print(request.form)
        if( "Is_Clone" not in request.form and "Not_Clone" not in request.form):
            flash('Kindly select an option')
            return redirect(url_for("home"))

        if ("Is_Clone" in request.form):
            update_true(current_userid, current_clone_no, app.users)
        if ("Not_Clone" in request.form):
            update_false(current_userid, current_clone_no, app.users)

        if ('Prev' in request.form):
            if(current_clone_no > 1):
                current_clone_no -= 1
                session['current_clone_no'] = current_clone_no
                return redirect(url_for("home"))

        if ('Next' in request.form):
            if(current_clone_no < 82):
                current_clone_no += 1
                session['current_clone_no'] = current_clone_no
                return redirect(url_for("home"))

        print(current_clone_no)
        
    return render_template("index.html", contents = contents, contents1 = contents1, clone_pair = str(current_clone_no), lines = lines, info=info)

@app.route('/login', methods=['GET','POST'])
def login():
    error = None
    if (request.method == 'POST'):
        if ( check_user(request.form['userid']) == False):
            error = 'Invalid userID. Please try again.'
        else:
            session['logged_in'] = True
            session['userid'] = request.form['userid']
            session['current_clone_no'] = 1
            g.db = connect_db()
            session['clone_pairs'] = clone_pairs(g.db, app.users, request.form['userid'])
            return redirect(url_for('home'))

    return render_template('login.html',error=error)

def check_user(user_id):
    return (user_id in app.users)

@app.route('/logout')
@login_required
def logout():
    session.pop('logged_in', None)
    session.pop('userid', None)
    session.pop('current_clone_no', None)
    session.pop('clone_pairs', None)
    return redirect(url_for('login'))


def connect_db():
    return sqlite3.connect(app.database)

if (__name__ == '__main__'):
    app.run(debug=True)