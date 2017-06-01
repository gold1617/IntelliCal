import httplib2
import os

from apiclient import discovery
from oauth2client import client
from oauth2client import tools
from oauth2client.file import Storage

from datetime import date,time,datetime,timedelta

from pytz import timezone

import dateutil.parser as tp

from flask import Flask,request,jsonify,session
import json

app = Flask(__name__)


json_file = open('paths.json').read()
paths = json.loads(json_file)
redir = paths['redir']
app.secret_key = paths['secret_key']
SCOPES = 'https://www.googleapis.com/auth/calendar'
CLIENT_SECRET_FILE = paths['client_secret']
APPLICATION_NAME = 'IntelliCal'

def get_credentials(authCode):
    
    if(session.get("credentials",None) == None or session.get('auth',None) != authCode):
        session['credentials'] = client.credentials_from_clientsecrets_and_code(CLIENT_SECRET_FILE,SCOPES,authCode,redirect_uri=redir).to_json()
        session['authCode'] = authCode
    
    return session['credentials']

@app.route('/calendars',methods=['POST'])
def calendars():
    server_auth = request.form['ServerAuth']

    credentials = client.OAuth2Credentials.from_json(get_credentials(server_auth))
    
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('calendar', 'v3', http=http)

    page_token = None
    while True:
        calendar_list = service.calendarList().list(pageToken=page_token,minAccessRole='writer').execute()
        for calendar_list_entry in calendar_list['items']:
            print( calendar_list_entry['summary'])
        page_token = calendar_list.get('nextPageToken')
        if not page_token:
            break

    return jsonify(calendar_list)

@app.route('/test')
def test():
    session['test'] = 'Test Successful'
    return session.get('test','failure')

@app.route('/test2')
def test2():
    res = session.get('test','Session not found')    
    return res


if __name__ == "__main__":
    app.run()
