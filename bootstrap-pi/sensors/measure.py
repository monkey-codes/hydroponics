from awscrt import mqtt
import sys
from uuid import uuid4
import json
import command_line_utils;
import psutil
import subprocess
from sense_hat import SenseHat

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
sense = SenseHat()

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


def get_sensor_data():
    load_avg = psutil.getloadavg()
    disk_usage = psutil.disk_usage('/')
    cpu_temp = psutil.sensors_temperatures(fahrenheit=False)
    cam_count = subprocess.check_output('lsusb | grep CAM | wc -l', shell=True, text=True)
    device_id = subprocess.check_output('cat /sys/class/net/eth0/address', shell=True, text=True).strip()
    rpi_model = subprocess.check_output('cat /sys/firmware/devicetree/base/model', shell=True, text=True).strip().replace('\u0000', '')
    temp = round(sense.get_temperature(),1)
    pres = round(sense.get_pressure(),1)
    hum = round(sense.get_humidity(),1)

    data = {
        "cpu_percent": psutil.cpu_percent(interval=1, percpu=False),
        "cpu_temp": cpu_temp['cpu_thermal'][0].current,
        "load_avg_1": load_avg[0],
        "load_avg_5": load_avg[1],
        "load_avg_15": load_avg[2],
        "mem_percent": psutil.virtual_memory().percent,
        "disk_usage_percent": disk_usage.percent,
        "disk_usage_used_mb": disk_usage.used//(1024*1024),
        "disk_usage_free_mb": disk_usage.free//(1024*1024),
        "webcam_count": int(cam_count.strip()),
        "temperature": temp,
        "barometric_pressure": pres,
        "humidity": hum,
        "device_id": device_id,
        "rpi_model": rpi_model

    }
    return data

if __name__ == '__main__':

    mqtt_connection = cmdUtils.build_mqtt_connection(on_connection_interrupted, on_connection_resumed)

    print("Connecting to {} with client ID '{}'...".format(
        cmdUtils.get_command(cmdUtils.m_cmd_endpoint), cmdUtils.get_command("client_id")), flush=True)
    connect_future = mqtt_connection.connect()

    # Future.result() waits until a result is available
    connect_future.result()
    print("Connected!", flush=True)

    message_topic = cmdUtils.get_command(cmdUtils.m_cmd_topic)
    mqtt_connection.publish(
        topic=message_topic,
        payload=json.dumps(get_sensor_data()),
        qos=mqtt.QoS.AT_LEAST_ONCE)

    # Disconnect
    print("Disconnecting...", flush=True)
    disconnect_future = mqtt_connection.disconnect()
    disconnect_future.result()
    print("Disconnected!", flush=True)
