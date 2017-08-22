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
    if(session.get("credentials",None) == None or session.get('authCode',None) != authCode):
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
    calendar_list_full = []
    while True:
        calendar_list = service.calendarList().list(pageToken=page_token,minAccessRole='writer').execute()
        for calendar_list_entry in calendar_list['items']:
            calendar_list_full.append(calendar_list_entry)
        page_token = calendar_list.get('nextPageToken')
        if not page_token:
            break
    return jsonify({'items':calendar_list_full})

@app.route('/addEvents',methods=['POST'])
def add_events():
    server_auth = request.form['ServerAuth']
    calid = request.form['CalID']
    td = float(request.form['Date'])
    timez = request.form['TZ']
    events = json.loads(request.form['Events'])
    days = int(request.form['Days'])

    credentials = client.OAuth2Credentials.from_json(get_credentials(server_auth))
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('calendar', 'v3', http=http)
    
    page_token = None
    calendar_list_full = []
    while True:
        calendar_list = service.calendarList().list(pageToken=page_token).execute()
        for calendar_list_entry in calendar_list['items']:
            calendar_list_full.append(calendar_list_entry)
        page_token = calendar_list.get('nextPageToken')
        if not page_token:
            break
    
    fail_to_add = 0
    for i in range(days):
        existing_events = []
        tz = timezone(timez)
        cdate = date.fromtimestamp(td)+timedelta(days=i)
        ctime = time(0,0)
        today = tz.localize(datetime.combine(cdate,ctime))#build today's datetime in local tz

        for calendar_list_entry in calendar_list['items']:#get all events occuring today
            eventsResult = service.events().list(
            calendarId=calendar_list_entry['id'], timeMin=today.isoformat(), timeMax=(today+timedelta(days=1)).isoformat(), singleEvents=True,
            orderBy='startTime').execute()
            for ev in eventsResult['items']:
                existing_events.append(ev)

        for event in events:
            if(not add_event(service,calid,cdate,existing_events,tz,timez,event[0],int(event[1]),event[2])):
                fail_to_add += 1

    return jsonify([fail_to_add])

def add_event(service,calid,cdate,events,tz,timez,summary,duration,start,buff=30):
    starth,startm = start.split(":")
    new_start_time = time(int(starth),int(startm))
    new_start_datetime = tz.localize(datetime.combine(cdate,new_start_time))
    new_end_datetime = new_start_datetime + timedelta(minutes=duration)
    
    adjusted = True
    adjusted_number = 0
    while adjusted and adjusted_number < 10 :
        adjusted = False
        for event in events:
            start = event['start'].get('dateTime')
            end = event['end'].get('dateTime')
            if start != None and end != None:
                if(new_start_datetime >= (tp.parse(start) - timedelta(minutes=buff)) and new_end_datetime <= (tp.parse(end) + timedelta(minutes=buff))) or ((new_start_datetime < (tp.parse(start) - timedelta(minutes=buff))) and (new_end_datetime > (tp.parse(end) + timedelta(minutes=buff)))):
                    if (tp.parse(end)+timedelta(minutes=buff)) - new_start_datetime < new_start_datetime - (tp.parse(start)-timedelta(minutes=buff)-timedelta(minutes=duration)):
                        new_start_datetime =  tp.parse(end) + timedelta(minutes=buff)
                        new_end_datetime = new_start_datetime + timedelta(minutes=duration)
                    else:
                        new_end_datetime = tp.parse(start)-timedelta(minutes=buff)
                        new_start_datetime = new_end_datetime - timedelta(minutes=duration)
                    adjusted = True
                    adjusted_number+=1
                elif new_start_datetime < (tp.parse(start) - timedelta(minutes=buff)) and new_end_datetime > (tp.parse(start) - timedelta(minutes=buff)) and new_end_datetime <= (tp.parse(end) + timedelta(minutes=buff)):
                    new_end_datetime = tp.parse(start) - timedelta(minutes=buff)
                    new_start_datetime = new_end_datetime - timedelta(minutes=duration)
                    adjusted = True
                    adjusted_number+=1
                elif new_start_datetime >= (tp.parse(start) - timedelta(minutes=buff)) and new_start_datetime < (tp.parse(end) + timedelta(minutes=buff)) and new_end_datetime > (tp.parse(end) + timedelta(minutes=buff)):
                    new_start_datetime = tp.parse(end) + timedelta(minutes=buff)
                    new_end_datetime = new_start_datetime + timedelta(minutes=duration)
                    adjusted = True
                    adjusted_number+=1

    if(adjusted_number > 10):
        return false

    event = {
            'summary': summary,
            'start': {
                'dateTime': new_start_datetime.isoformat(),
                'timeZone': timez,
                },
            'end': {
                'dateTime': new_end_datetime.isoformat(),
                'timeZone': timez,
                },
            'reminders': {
                'useDefault': True,
                },
            }

    event = service.events().insert(calendarId=calid,body=event).execute()
    return event.get('htmlLink',None) != None

        


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
