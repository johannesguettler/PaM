/**
 * @author  Christian Schönweiß, University Freiburg
 * @since   09.02.2015
 * 
 */

package Server;

import android.annotation.TargetApi;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * <h1>Communication Thread</h1> Every Monitor connecting to Server gets its own
 * Communication Thread to communicate to Controller and vice versa using
 * out()-method or (not used yet) in()-method
 * 
 */
class CommunicationThread extends Thread {

	//private final Thread discoverThread;
	// given socket connection of Server
	private  Socket socket;
  private final Server server;

  // if new message is ready to send
	private boolean newMessage = true;
	// not used yet, but if communication to Monitor should be vice versa
	private String incomeMessage = "";
  // Mutex Variables for Thread Concurrency
  // Mutex for Connection and Discovery Thread
  private final Object lockConnect = new Object();
  private final Object lockDiscovery = new Object();
  private int clientPort = 0;
  private InetAddress clientIp;
	private boolean exitThread;

	// -- GETTER / SETTER --

	public Socket getSocket() {
		return socket;
	}

	public String getIncomeMessage() {
		return incomeMessage;
	}

	public void setIncomeMessage(String incomeMessage) {
		this.incomeMessage = incomeMessage;
	}

	public boolean isNewMessage() {
		return newMessage;
	}

	public void setNewMessage(boolean newMessage) {
		this.newMessage = newMessage;
	}

	// -- END GETTER / SETTER --

	/**
	 * 
	 * CONSTRUCTOR Creates a Communication Thread for every connected Client
	 * based on the given Socket connection
	 * 
	 * @param socket
	 *            Socket (Client) given after Monitor connects to Server
	 * 
	 */
	CommunicationThread(Socket socket, Server server) {
		this.socket = socket;
    this.server = server;
		clientPort = socket.getPort();
		clientIp = socket.getInetAddress();
/*		this.discoverThread = new Thread(new DiscoveryThread());
		this.discoverThread.start();
    this.doNotifyOnDiscovery();*/

    //doNotifyOnConnect();
	}
	/**
	 * Locks Mutex "lockConnect" for Connection Thread waiting for Input of
	 * user or Discovery Thread
	 */
	private void doWaitOnConnect() {
		synchronized (lockConnect) {
			try {
				lockConnect.wait();
				Log.d("DEBUG CommThread",
						"ConnectionThread waiting for Input or DiscoveryThread");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
  /**
   * Notify Discovery Thread to continue
   */
  void doNotifyOnDiscovery() {
    synchronized (lockDiscovery) {
      lockDiscovery.notify();
      String TAG = "MUTEX Discovery";
      Log.d(TAG, "Wakeup ConnectionThread");
    }
  }
	public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      //doWaitOnConnect();
      try {
        while (!Thread.currentThread().isInterrupted()) {
          /*if (getSocket() == null) {
            setSocket(new Socket(clientIp, clientPort));
            Log.d("DEBUG CommThread.run", "Client connected");
            Log.d("DEBUG CommThread.run",
                "Connected to "
                    + clientIp.getHostAddress()
                    + " on port " + clientPort);

          } else {
            Log.d("DEBUG CommThread.run", "Socket already initialized. SKIPPED");
          }*/
          incomeMessage = in();
          /*Log.e("DEBUG CommThread", "incoming message (unfiltered): " + "" +
							incomeMessage);*/

          if (incomeMessage.length() != 0) {
            server.in(incomeMessage);
            Log.e("DEBUG CommThread", "incomimng message: " + incomeMessage);
          }


        }
      } catch (Exception e1) {
        e1.printStackTrace();
        Log.e("DEBUG CommThread.run", "Unknown Host");
      }
    }
		if(exitThread) {
			this.interrupt();
		}
  }
  /**
   * Sets socket after a socket connection was established
   * closes socket if a socket exists before and creates a new one
   * @param socket Established socket connection to Controller
   */
  public synchronized void setSocket(Socket socket) {

    if (socket == null) {

    }
    if (this.socket != null) {
      if (this.socket.isConnected()) {
        try {
          this.socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    this.socket = socket;
  }
	/**
	 * @return Message from other device (Monitor) as string
	 */
	public String in() {
		String inStr = "";
		StringBuilder completeString =
				new StringBuilder();
    DataInputStream dataInputStream = null;
		try {
			BufferedReader inMessage = new BufferedReader(new
					InputStreamReader(socket.getInputStream()));

			while ((inStr = inMessage.readLine()) != null) {
				completeString.append(inStr);
				if (inStr.contains("}"))
					break;
			}
/*      dataInputStream = new DataInputStream(
          socket.getInputStream());
      inStr = dataInputStream.readUTF();*/
		} catch (IOException e) {
			e.printStackTrace();
		}
		return completeString.toString();
	}

	/**
	 * 
	 * Sends JSON-String to Monitor using BufferedWriter and given Socket
	 * connection
	 * 
	 * @param jsonString
	 *            JSON-String is build by GSON Builder, send to Monitor
	 * @throws IOException
	 *             exceptions for Socket, flush or writer Errors
	 * 
	 */
	void out(String jsonString) throws IOException {
		try {

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			writer.write(jsonString + "\n");
			writer.flush();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void exit() {
		exitThread = true;
	}


	private @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	class DiscoveryThread extends Thread {

		// Server Client Connection using HTTP over TCP for JSON transmission
		private static final String SERVICE_TYPE = "_http._tcp.";

		// NSD Manager contains a Listener for discovery of Monitoring Service
		// in the Network
		private NsdManager.DiscoveryListener mDiscoveryListener;

		// NSD Manager contains a Listener for resolvement of Monitoring Service
		// in the Network
		private NsdManager.ResolveListener mResolveListener;
		private final String TAG = "DiscoveryThread";

		// Manager for Android Network Discovery on Monitor
		private NsdManager mNsdManager;

		// NSD Service Info for Monitoring service discovery and resolvement
		private NsdServiceInfo mService;

		/**
		 * Service discovery starts based on initialized Listener and service
		 * type http.tcp
		 */
		private void discoverServices() {
			mNsdManager.discoverServices(SERVICE_TYPE,
					NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
		}

		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {

					doWaitOnDiscovery();

					// Initialize NSD Manager using MainActivity Context
					mNsdManager = server.getmNsdManager();

					// Initialize Listener for Resolve Monitoring Service
					initializeResolveListener();
					Log.d(TAG, "Initializing of ResolveListener DONE");

					// Initialize Listener for Discovery Monitoring Service
					initializeDiscoveryListener();
					Log.d(TAG, "Initializing of DiscoveryListener DONE");

					// After initialization discover Monitoring service in the
					// network
					discoverServices();
				} catch (Exception e1) {
					e1.printStackTrace();
					Log.e(TAG, "No Host found");
				}
			}
		}

		/**
		 * Initialize Listener for Discovery of Monitoring Service
		 */
		private void initializeDiscoveryListener() {

			// Instantiate a new DiscoveryListener
			mDiscoveryListener = new NsdManager.DiscoveryListener() {

				// Called as soon as service discovery begins.
				@Override
				public void onDiscoveryStarted(String regType) {
					Log.d(TAG, "Service discovery started");
				}

				@Override
				public void onServiceFound(NsdServiceInfo service) {
					// Service was found
					Log.d(TAG, "Service discovery success" + service);
					if (!service.getServiceType().equals(SERVICE_TYPE)) {
						// Service type is the string containing the protocol
						// and
						// transport layer for this service.
						Log.d(TAG,
								"Unknown Service Type: "
										+ service.getServiceType());

					} else if (service.getServiceName().equals(server.getServiceName())) {
						// The name of the service was found in the network
						mNsdManager.resolveService(service, mResolveListener);
						Log.d(TAG, "Service found: " + server.getServiceName());
					}
				}

				@Override
				public void onServiceLost(NsdServiceInfo service) {
					// When the network service is no longer available.
					Log.e(TAG, "service lost" + service);
				}

				@Override
				public void onDiscoveryStopped(String serviceType) {
					Log.i(TAG, "Discovery stopped: " + serviceType);
				}

				// Stops Service Discovery if discovery fails at the start
				@Override
				public void onStartDiscoveryFailed(String serviceType,
																					 int errorCode) {
					Log.e(TAG, "Discovery failed: Error code:" + errorCode);
					mNsdManager.stopServiceDiscovery(this);
				}

				// Stops Service Discovery if discovery fails at the end
				@Override
				public void onStopDiscoveryFailed(String serviceType,
																					int errorCode) {
					Log.e(TAG, "Discovery failed: Error code:" + errorCode);
					mNsdManager.stopServiceDiscovery(this);
				}
			};
		}

		/**
		 * Initialize Listener for Resolvement of Monitoring Service
		 */
		private void initializeResolveListener() {
			mResolveListener = new NsdManager.ResolveListener() {

				@Override
				public void onResolveFailed(NsdServiceInfo serviceInfo,
																		int errorCode) {
					// Called when the resolve fails
					Log.e(TAG, "Resolve failed" + errorCode);
				}

				@Override
				public void onServiceResolved(NsdServiceInfo serviceInfo) {
					Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
					mService = serviceInfo;
          clientIp = mService.getHost();
          clientPort = mService.getPort();
					doNotifyOnConnect();
				}
			};
		}
	}

	/**
	 * Notify ConnectionThread to continue
	 */
	void doNotifyOnConnect() {
		synchronized (lockConnect) {

			try {
				getSocket().shutdownInput();
				getSocket().close();
			} catch (Exception e) {
				System.err.println(e);
			}
			socket = null;
			lockConnect.notify();
			String TAG = "Lock Connection";
			Log.d(TAG, "Wakeup ConnectionThread");
		}
	}

	/**
	 * DiscoveryThread waits for User Input
	 */
	private void doWaitOnDiscovery() {
		synchronized (lockDiscovery) {
			try {
				lockDiscovery.wait();
				String TAG = "Lock Discovery";
				Log.d(TAG, "DiscoveryThread waiting for Input of User");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}


