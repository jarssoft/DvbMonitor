# DvbMonitor

Extracts teletext and other stuff from MPEG Transport Stream in real-time or from local files.

- PacketReader.*: Reads common transport stream packets. Can be filtering by PID-value.
- Teletext.*: A simple teletext decoder.
- Teletext.SubtitleMonitor: Views teletext subtitles like a chat.
- EPG.*: Decodes Event Information Table
- TODO: View thumbnails from video.
- TODO: Listen audio only television.
- TODO: Decode also adaptation fields in TS-packets.
 
## Installation and using

DvbMonitor runs on Java Runtime Environment (at least OpenJDK 11 and 17) on the Linux system. To receive television signal as Transport Stream you need the device and software for tuning.

#### Tuning (optional)
If you want to receive stream from aerial or cable broadcast, you need to make 'zapping'. Before it, scan channels. This example uses dvb-tools.

<pre>
dvbv5-scan /usr/local/share/dvb/scan/dvb-t/fi-Eurajoki -o channels-v5.conf
</pre>
<pre>
dvbv5-zap -c channels-v5.conf 610000000 -P 
</pre>

You need to use -P to get all PIDs of the stream. Now, the dvb stream is located in /dev/dvb/adapter0/dvr0.

#### Using DvbMonitor

First, compile the java-files:

<pre>
javac PacketReader/Monitor.java
</pre>

If you are receive stream in real-time, pipe the streamfile to program.

<pre>
cat /dev/dvb/adapter0/dvr0 |java PacketReader.Monitor
</pre>

On using slow hardware, the stream can overflows. Use [dvbsnoop](https://dvbsnoop.sourceforge.net/) to filter PIDS and make the processing faster:

<pre>
dvbsnoop -s ts -b 0x12 |java EPG.Monitor
</pre>

(*PIDS defines content of packets, like video or teletext. 0x12 stands for event information table.)

To read local file:

<pre>
cat ../test.ts |java PacketReader.Monitor
</pre>

## SubtitleMonitor

Here is an example of reading subtitles from multiple television channel and put them like a chat. It also grouping text lines to paragraphs. It use header format <Channel Name / Program Name>.

![Screenshot](https://www.ohjelmakartta.fi/github/screenshot.jpg)

## Read more

- [Wikipedia: MPEG_transport_stream](https://en.wikipedia.org/wiki/MPEG_transport_stream)

- [ETSI EN 300 706 Enhanced Teletext specification](https://www.etsi.org/deliver/etsi_en/300700_300799/300706/01.02.01_60/en_300706v010201p.pdf),  V1.2.1 (2003-04),  European Telecommunications Standards Institute

- [ETSI EN 300 468 Digital Video Broadcasting (DVB); Specification for Service Information (SI) in DVB systems](https://www.etsi.org/deliver/etsi_en/300400_300499/300468/01.17.01_20/en_300468v011701a.pdf), V1.17.1 (2022-07)
