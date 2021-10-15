'use strict';

const PahoMqttClient = () => {

	const [ clientId, setClientId ] = React.useState("hungfromhktest20210901")
	const [ host, setHost ] = React.useState("1c3b21dd0317425d916b4ed67fcd4309.s1.eu.hivemq.cloud");
	const [ port, setPort ] = React.useState(8884);
	const [ username, setUsername ] = React.useState("user1");
	const [ password, setPassword ] = React.useState("Uw8LgB66YpjYujS");
	
	//const [ message, setMessage ] = React.useState("a\nb");

	const logmsg = (message) => {
		document.querySelector("#messages").value += `${message}\n`;
	}
	
	const bindToBroker = () => {
		document.querySelector("#messages").value="";
		
		const client = new Paho.MQTT.Client(host, port, clientId);
		
		client.onConnected = (reconnect,uri) => {
			logmsg(`mqtt client connected to ${uri} ${reconnect}`);
		};
		
		client.onConnectionLost = (resp) => {
			logmsg(`onConnectionLost: ${resp.errorCode}:${resp.errorMessage}`)	
		};
		
		client.onMessageArrived = (msg) => {
			logmsg(`message [${msg.payloadString}] received from topic [${msg.destinationName}]`);
			let qos = 1; //0-best effort, 1-at least once, 2-exactly once
			client.startTrace();
			client.send("hungtest-reply",`Reply from ${client.clientId}`,qos);
			client.stopTrace();
			console.log(client.getTraceLog());
		};
		
		client.onMessageDelivered = (msg) => {
			logmsg(`message [${msg.payloadString}] sent to topic [${msg.destinationName}]`);
		};
		
		//Connect to test.mosquitto.org with secured websocket protocol, 
		//otherwise will be blocked by browser security policy
		client.connect({
			useSSL: true,
			userName: username,
			password: password,
			mqttVersion: 4, //3-MQTT V3.1, 4-MQTT V3.1.1
			onSuccess: (ctx) => {
				logmsg("mqtt client successfully connected");
				client.subscribe("hungtest-send", {
					onSuccess: () => {
						logmsg("subscribe success");
					},
					onFailure: () => {
						logmsg("subscribe failure");
					}
				});
			},
			onFailure: (ctx,errCode,errMsg) => {
				logmsg(`mqtt client connection failure: ${errCode}:${errMsg}`)
			},
		});		
	}
	
	return (
		<div className="card my-card-view">
			<div className="mb-3 row">
				<label htmlFor="clientId"
					className="col-sm-2 col-form-label">Client Id
				</label>
				<div className="col-sm-10">
					<input id="clientId"
						className="form-control"
						value={clientId}
						onChange={setClientId}/>
				</div>
			</div>
					
			<div className="mb-3 row">
				<label htmlFor="host"
					className="col-sm-2 col-form-label">Broker host
				</label>
				<div className="col-sm-10">
					<input id="host"
						className="form-control"
						value={host}
						onChange={setHost}/>
				</div>
			</div>
			
			<div className="mb-3 row">
				<label htmlFor="port"
					className="col-sm-2 col-form-label">Broker port
				</label>
				<div className="col-sm-10">	
					<input id="port"
						className="form-control"
						value={port}
						onChange={setPort}/>
				</div>
			</div>
			
			<div className="mb-3 row">
				<label htmlFor="username"
					className="col-sm-2 col-form-label">Username
				</label>
				<div className="col-sm-10">
					<input id="username"
						className="form-control"
						value={username}
						onChange={setUsername}/>
				</div>
			</div>
			
			<div className="mb-3 row">
				<label htmlFor="password"
					className="col-sm-2 col-form-label">Password
				</label>
				<div className="col-sm-10">
					<input id="password"
						className="form-control"
						value={password}
						onChange={setPassword}/>
				</div>
			</div>
			
			<div className="btn-group">
				<a href="http://www.hivemq.com/demos/websocket-client/"
					className="btn btn-primary" target="_blank">
					Open Hive WebSocket Client
				</a>
				<button className="btn btn-primary" 
					onClick={bindToBroker}>Bind to Broker</button>
			</div>

			<div className="mb-3">
				<label htmlFor="messages" className="form-label">Messages</label>
				<textarea className="form-control" id="messages" rows="10" readOnly>
				</textarea>
			</div>
		</div>
	);
}

const domContainer = document.querySelector('#main');
ReactDOM.render(<PahoMqttClient/>, domContainer);