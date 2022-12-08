from awscrt import mqtt
import sys
import threading
import time
from uuid import uuid4
import json
import socket
import os, os.path
import time
from collections import deque
import requests
import command_line_utils;
# Parse arguments
cmdUtils = command_line_utils.CommandLineUtils("PubSub - Send and recieve messages through an MQTT connection.")
cmdUtils.add_common_mqtt_commands()
cmdUtils.add_common_topic_message_commands()
cmdUtils.add_common_proxy_commands()
cmdUtils.add_common_logging_commands()
cmdUtils.register_command("key", "<path>", "Path to your key in PEM format.", True, str)
cmdUtils.register_command("cert", "<path>", "Path to your client certificate in PEM format.", True, str)
cmdUtils.register_command("port", "<int>", "Connection port. AWS IoT supports 443 and 8883 (optional, default=auto).", type=int)
cmdUtils.register_command("client_id", "<str>", "Client ID to use for MQTT connection (optional, default='test-*').", default="test-" + str(uuid4()))
cmdUtils.register_command("count", "<int>", "The number of messages to send (optional, default='10').", default=10, type=int)
# Needs to be called so the command utils parse the commands
cmdUtils.get_args()

received_count = 0
received_all_event = threading.Event()

# Callback when connection is accidentally lost.
def on_connection_interrupted(connection, error, **kwargs):
    print("Connection interrupted. error: {}".format(error), flush=True)


# Callback when an interrupted connection is re-established.
def on_connection_resumed(connection, return_code, session_present, **kwargs):
    print("Connection resumed. return_code: {} session_present: {}".format(return_code, session_present), flush=True)

    if return_code == mqtt.ConnectReturnCode.ACCEPTED and not session_present:
        print("Session did not persist. Resubscribing to existing topics...", flush=True)
        resubscribe_future, _ = connection.resubscribe_existing_topics()

        # Cannot synchronously wait for resubscribe result because we're on the connection's event-loop thread,
        # evaluate result with a callback instead.
        resubscribe_future.add_done_callback(on_resubscribe_complete)


def on_resubscribe_complete(resubscribe_future):
    resubscribe_results = resubscribe_future.result()
    print("Resubscribe results: {}".format(resubscribe_results), flush=True)

    for topic, qos in resubscribe_results['topics']:
        if qos is None:
            sys.exit("Server rejected resubscribe to topic: {}".format(topic))


# Callback when the subscribed topic receives a message
def on_message_received(topic, payload, dup, qos, retain, **kwargs):
    print("Received message from topic '{}': {}".format(topic, payload), flush=True)
    message = json.loads(payload)
    if message['type'] == 'response':
        upload_file(message)
    global received_count
    received_count += 1
    if received_count == cmdUtils.get_command("count"):
        received_all_event.set()

def listen_for_upload_requests(mqtt_connection):
    # listen for messages on unix domain socket
    socket_name = "/home/hydro/upload.sock"
    if os.path.exists(socket_name):
        os.remove(socket_name)
    server = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    server.bind(socket_name)
    while True:
      server.listen(1)
      conn, addr = server.accept()
      datagram = conn.recv(4096)
      if datagram:
        tokens = datagram.decode().strip().split(":")
        print(datagram, flush=True)
        print("File: {} Key: {}".format(tokens[0], tokens[1]), flush=True)
        send_upload_request(mqtt_connection, tokens[0], tokens[1])
      conn.close()

def send_upload_request(mqtt_connection, file, key):
    request = {"type": "request", "file": file, "key": key}
    message_topic = cmdUtils.get_command(cmdUtils.m_cmd_topic)
    mqtt_connection.publish(
        topic=message_topic,
        payload=json.dumps(request),
        qos=mqtt.QoS.AT_LEAST_ONCE)

def upload_file(message):
    file = message['file']
    url = message['url']
    data = open(file, 'rb').read()
    headers = {}
    upload = requests.put(url,data=data,headers=headers)
    print(str(upload.content), flush=True)

if __name__ == '__main__':

    mqtt_connection = cmdUtils.build_mqtt_connection(on_connection_interrupted, on_connection_resumed)

    print("Connecting to {} with client ID '{}'...".format(
        cmdUtils.get_command(cmdUtils.m_cmd_endpoint), cmdUtils.get_command("client_id")), flush=True)
    connect_future = mqtt_connection.connect()

    # Future.result() waits until a result is available
    connect_future.result()
    print("Connected!", flush=True)

    message_count = cmdUtils.get_command("count")
    message_topic = cmdUtils.get_command(cmdUtils.m_cmd_topic)

    # Subscribe
    print("Subscribing to topic '{}'...".format(message_topic), flush=True)
    subscribe_future, packet_id = mqtt_connection.subscribe(
        topic=message_topic,
        qos=mqtt.QoS.AT_LEAST_ONCE,
        callback=on_message_received)

    subscribe_result = subscribe_future.result()
    print("Subscribed with {}".format(str(subscribe_result['qos'])), flush=True)
    if message_count != 0 and not received_all_event.is_set():
        print("Waiting for all messages to be received...", flush=True)


    listen_for_upload_requests(mqtt_connection)

    received_all_event.wait()
    print("{} message(s) received.".format(received_count), flush=True)

    # Disconnect
    print("Disconnecting...", flush=True)
    disconnect_future = mqtt_connection.disconnect()
    disconnect_future.result()
    print("Disconnected!", flush=True)
