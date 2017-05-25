
from __future__ import print_function
import httplib2
import os

from apiclient import discovery
from oauth2client import client
from oauth2client import tools
from oauth2client.file import Storage

from datetime import date,time,datetime,timedelta

from pytz import timezone

import dateutil.parser as tp


# If modifying these scopes, delete your previously saved credentials
# at ~/.credentials/calendar-python-quickstart.json
SCOPES = 'https://www.googleapis.com/auth/calendar'
CLIENT_SECRET_FILE = 'client_secret.json'
APPLICATION_NAME = 'Google Calendar API Python Quickstart'


def get_credentials():#use server auth from phone
    """Gets valid user credentials from storage.

    If nothing has been stored, or if the stored credentials are invalid,
    the OAuth2 flow is completed to obtain the new credentials.

    Returns:
        Credentials, the obtained credential.
    """
    home_dir = os.path.expanduser('~')
    credential_dir = os.path.join(home_dir, '.credentials')
    if not os.path.exists(credential_dir):
        os.makedirs(credential_dir)
    credential_path = os.path.join(credential_dir,
                                   'calendar-python-quickstart.json')

    store = Storage(credential_path)
    credentials = store.get()
    if not credentials or credentials.invalid:
        flow = client.flow_from_clientsecrets(CLIENT_SECRET_FILE, SCOPES)
        flow.user_agent = APPLICATION_NAME
        if flags:
            credentials = tools.run_flow(flow, store, flags)
        else: # Needed only for compatibility with Python 2.6
            credentials = tools.run(flow, store)
        print('Storing credentials to ' + credential_path)
    return credentials

def main():
    """Shows basic usage of the Google Calendar API.

    Creates a Google Calendar API service object and outputs a list of the next
    10 events on the user's calendar.
    """
    credentials = get_credentials()
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('calendar', 'v3', http=http)
    
    page_token = None
    while True:
        calendar_list = service.calendarList().list(pageToken=page_token).execute()
        for calendar_list_entry in calendar_list['items']:
            print( calendar_list_entry['summary'])
        page_token = calendar_list.get('nextPageToken')
        if not page_token:
            break
    
    cal = raw_input('\n\nWhich Calendar do you want to use: ')
    calitem = [value for value in calendar_list['items'] if value['summary']==cal]

    if(calitem == []):
        return

    calid = calitem[0]['id']#get from phone
    timez = 'US/Eastern'#get from phone
    cdate = date.today()#get from phone
    ctime = time(0,0)
    tz = timezone(timez)
    today = tz.localize(datetime.combine(cdate,ctime))#build today's datetime in local tz
    print('Getting today\'s events')
    events = []

    for calendar_list_entry in calendar_list['items']:#get all events occuring today
        eventsResult = service.events().list(
            calendarId=calendar_list_entry['id'], timeMin=today.isoformat(), timeMax=(today+timedelta(days=1)).isoformat(), singleEvents=True,
            orderBy='startTime').execute()
        for ev in eventsResult['items']:
            events.append(ev)

    '''if not events:
        print('No upcoming events found.')
    for event in events:
        start = event['start'].get('dateTime')
        print(start, event['summary'])'''
    
    buff = 30
    duration = 60#varies by event; comes from phone
    new_summmary = 'lunch'
    new_start_time = time(12,30)
    new_start_datetime = tz.localize(datetime.combine(cdate,new_start_time))
    new_end_datetime = new_start_datetime + timedelta(minutes=duration)
    
    adjusted = True
    while adjusted:
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
                elif new_start_datetime < (tp.parse(start) - timedelta(minutes=buff)) and new_end_datetime > (tp.parse(start) - timedelta(minutes=buff)) and new_end_datetime <= (tp.parse(end) + timedelta(minutes=buff)):
                    new_end_datetime = tp.parse(start) - timedelta(minutes=buff)
                    new_start_datetime = new_end_datetime - timedelta(minutes=duration)
                    adjusted = True
                elif new_start_datetime >= (tp.parse(start) - timedelta(minutes=buff)) and new_start_datetime < (tp.parse(end) + timedelta(minutes=buff)) and new_end_datetime > (tp.parse(end) + timedelta(minutes=buff)):
                    new_start_datetime = tp.parse(end) + timedelta(minutes=buff)
                    new_end_datetime = new_start_datetime + timedelta(minutes=duration)
                    adjusted = True



    event = {
            'summary': new_summmary,
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
    event= service.events().insert(calendarId=calid,body=event).execute()
    print('Event created: %s' % (event.get('htmlLink')))
    

if __name__ == '__main__':
    main()
