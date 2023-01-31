# DvbMonitor

Extracts teletext and other stuff from MPEG Trasport Stream binary data.

- Monitoring teletext with many special data. (Monitor-class)
- View teletext subtitles like a chat. (SubtitleMonitor-class)
- Reads EPG initially
- TODO: read thumbnails from video
- TODO: read audio only

## Installation

DvbMonitor runs on Java Runtime Environment on the Linux system. To receive television signal as Transport Stream you need the device and software for tuning.

## Using

Make 'zapping'. For example run the dvb-zap from dvb-tools. You need to use -P to get all PIDs of the stream. For example:

<pre>
dvbv5-zap -c channels-v5.conf 610000000 -P 
</pre>

Then use /dev/dvb/adapter0/dvr0 as standard input,

<pre>
cat /dev/dvb/adapter0/dvr0 |java -ea Monitor
</pre>

...or use [dvbsnoop](https://dvbsnoop.sourceforge.net/) to get only one PID,

<pre>
dvbsnoop -s ts -b 5000 |java -ea Monitor
</pre>

...or give it an example transport stream from file:

<pre>
cat ../test.ts |java -ea Monitor
</pre>

## SubtitleMonitor

Here is an example of reading subtitles from multiple television channel and put them like a chat. It also grouping text lines to paragraphs. It use header format <Channel Name / Program Name>.

![Screenshot](https://www.ohjelmakartta.fi/github/screenshot.jpg)
