import httplib2
import os

from apiclient import discovery
from oauth2client import client
from oauth2client import tools
from oauth2client.file import Storage

from datetime import date,time,datetime,timedelta

from pytz import timezone

import dateutil.parser as tp


from flask import Flask,request,jsonify
import json

app = Flask(__name__)

json_file = open('paths.json').read()
paths = json.loads(json_file)
redir = paths['redir']

SCOPES = 'https://www.googleapis.com/auth/calendar'
CLIENT_SECRET_FILE = paths['client_secret']
APPLICATION_NAME = 'IntelliCal'

def get_credentials(authCode):
    credentials = client.credentials_from_clientsecrets_and_code(CLIENT_SECRET_FILE,SCOPES,authCode,redirect_uri=redir)
    
    return credentials

@app.route('/calendars',methods=['POST'])
def calendars():
    server_auth = request.form['ServerAuth']

    credentials = get_credentials(server_auth)
    
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
    return 'Test Successful'


if __name__ == "__main__":
    app.run()
