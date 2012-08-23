#!/bin/bash
java -Djava.awt.headless=true -cp nano-remexp.jar nano.remexp.broadcaster.RemoteExperimentBroadcaster &> exception.log

