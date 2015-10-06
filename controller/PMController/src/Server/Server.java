/**
 * @author Christian Schönweiß, University Freiburg
 * @since 09.02.2015
 */

package Server;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import gui.MainActivity;

/**
 * <h1>Server (Server Socket)</h1>
 * Every Server holds a Server Thread and a CommunicationThread
 * to communicate over TCP and JSON with its out()-Method to a Monitor
 */
public class Server {

  private final MainActivity mainActivity;
  private ServerSocket serverSocket;
  private ServerThread serverThreadInstance = null;
  private Thread serverThread = null;

  // private CommunicationThread commThread;

  // List of CommunicationThreads
  private ArrayList<CommunicationThread> commThreads = new ArrayList<CommunicationThread>();

  // Different debugging tags for LogCat
  private final String tagST = "ServerThread";
  private final String tagS = "Server";
  private final String tagNC = "Network";

  // Manager for Android Network Discovery on Controller
  private NsdManager mNsdManager;
  private RegistrationListener mRegistrationListener;
  private String serviceName;
  private NsdServiceInfo mServiceInfo;

  // Hardcoded controller port because of usability (Task of network admin to
  // open port 3000 //TODO: change comment: no hardcoded port
  private int controllerPort = 0;

  // -- GETTER / SETTER --

  public NsdManager getmNsdManager() {
    return mNsdManager;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public int getControllerPort() {
    return controllerPort;
  }

  // -- End GETTER / SETTER --

  /**
   * Creates a Server object to set Service name of NSD Manager and starts the
   * Server Thread to register Service under the given Service name
   *
   * @param serviceName  User input: Service Name was set by user before
   * @param mainActivity
   */
  public Server(String serviceName, MainActivity mainActivity) {
    this.mainActivity = mainActivity;
    this.serviceName = serviceName;
    this.serverThreadInstance = new ServerThread(this);
    this.serverThread = new Thread(serverThreadInstance);
    this.serverThread.start();
  }

  /**
   * Interrupts Thread and closes Server Socket connection
   */
  public void tearDownServer() {
    serverThread.interrupt();
    for (CommunicationThread commThread: commThreads) {
      commThread.exit();
    }
    commThreads.clear();

    try {
      serverSocket.close();
    } catch (IOException ioe) {
      Log.e(tagS, "Error when closing server socket.");
    }
  }

  /**
   * Sends JSONstring to every Monitor over its socket connection
   * (CommunicationThread)
   *
   * @param jsonString JSON-String created by an event before sending
   */
  public void out(String jsonString) {
    //TODO: new connection procedure
    //serverThreadInstance.setOutputString(jsonString);

    for (CommunicationThread commThread : commThreads) {
      try {
        if (commThread == null) {
          Log.d(tagS, "No Communiction Handler initialized");
          return;//TODO: wenn ein monitor verschweindet wird das system
          // lahmgelegt?
        }
        //Log.d(tagS, "Controller sends: " + jsonString);
        commThread.out(jsonString);
      } catch (IOException e) {
        System.err.println("Caught IOException: " + e.getMessage());
      }
    }
  }

  /**
   * Forward incoming message from active monitor to main program
   *
   * @param jSonString
   */
  public void in(String jSonString) {
    mainActivity.incomingMonitorEvent(jSonString);
  }


  /**
   * <h1>Server Thread</h1> The Server Thread establishs a ServerSocket on a
   * hardcoded Port (because of usability for user) and registers a Network
   * Service via NSD Manager in its network Ever new Client connecting to
   * Server gets its own Thread to communicate with the Server Socket via
   * Socket connection
   */
  private class ServerThread implements Runnable {
    private final Server parentServer;
    private String outputString = "";
    private ArrayList<InetAddress> clientInetAdressList;

    public ServerThread(Server server) {
      super();
      this.parentServer = server;
      clientInetAdressList = new ArrayList<>();
    }

    public void run() {
      Socket socket = null;
      try {
        // Initialize a server socket on some available port
        serverSocket = new ServerSocket(0);
        // get chosen port
        controllerPort = serverSocket.getLocalPort();
      } catch (Exception e) {
        System.err.println("Caught IOException: " + e.getMessage());
      }
      // Register Service in the Network over NSDManager
      registerService(controllerPort);
      Log.d(tagST, "Server running on port: " + getControllerPort());

      while (!Thread.currentThread().isInterrupted()) {
        //TODO: new connection procedure
        try {
          socket = serverSocket.accept();
          // if unknown add client to list and start communication thread



          InetAddress clientInetAdress = socket.getInetAddress();
          if (!clientInetAdressList.contains(clientInetAdress)) {

            CommunicationThread commThread = (new CommunicationThread
                (socket, parentServer));
            commThreads.add(commThread);
            clientInetAdressList.add(clientInetAdress);
            Log.e("DEBUG Server", "new client added: "+clientInetAdress+". " +
                "number of comthreads: " + commThreads.size());
            commThread.start();
          } else {
            setNewSocket(clientInetAdress, socket);
          }

          // begin incoming message
          /*String inStr = "";
          StringBuilder completeString =
              new StringBuilder();

          try {
            InputStreamReader inputStreamReader = new InputStreamReader
                (socket.getInputStream());
            BufferedReader inMessage = new BufferedReader(inputStreamReader);

            while (((inStr = inMessage.readLine()) !=
                null)) {
               completeString.append(inStr);
              if (inStr.contains("}"))
                break;
            }
            inStr = completeString.toString();
          } catch (IOException e) {
            e.printStackTrace();
          }
          if (!inStr.isEmpty()) {
            parentServer.in(inStr);
            Log.e("DEBUG ServerThread", "incomimng message: " + inStr);
          }*/
          // end incoming message

        } catch (IOException e) {
          e.printStackTrace();
        } /*finally {
          if (socket != null){
            try {
              socket.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }*/

        /*try {
          // starts Communication Thread for every new client
          // connecting to Server
          CommunicationThread commThread = (new CommunicationThread
              (serverSocket.accept(),
              parentServer));
          Log.e(tagST, "Client connected");
          if (commThreads.size() == 0) {
            commThreads.add(commThread);
            Log.e("DEBUG Server", "new client added. number of comthreads: " +
                "" + commThreads.size());
            commThread.start();
          }

          Log.d(tagST, "Connection Handler started");
        } catch (IOException e1) {
          System.err
              .println("Caught IOException: " + e1.getMessage());
        } catch (NullPointerException e2) {
          System.err.println("Caught NullPointerException: "
              + e2.getMessage());
        }*/
      }
    }

    private void setNewSocket(InetAddress clientInetAdress, Socket socket) {
      for (CommunicationThread communicationThread:commThreads) {
        if (communicationThread.getSocket().getInetAddress().equals
            (clientInetAdress)) {
          communicationThread.setSocket(socket);
          break;
        }
      }
    }

    public void setOutputString(String outputString) {
      this.outputString = outputString;
    }
  }

  /**
   * Gets controllerPort from ServerSocket to register Service in the Network
   * over NsdServiceInfo uses HTTP over TCP
   */
  private void registerService(int port) {
    mServiceInfo = new NsdServiceInfo();
    mServiceInfo.setServiceName(serviceName);
    mServiceInfo.setServiceType("_http._tcp.");
    mServiceInfo.setPort(port);

    mNsdManager = (NsdManager) MainActivity.getAppContext()
        .getSystemService(Context.NSD_SERVICE);

    initializeRegistrationListener();

    // registers Service with given parameters
    mNsdManager.registerService(mServiceInfo, NsdManager.PROTOCOL_DNS_SD,
        mRegistrationListener);
  }

  /**
   * Listener for events on registration of service
   */
  private void initializeRegistrationListener() {
    mRegistrationListener = new NsdManager.RegistrationListener() {

      @Override
      public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
        serviceName = NsdServiceInfo.getServiceName();
        Log.d(tagNC, "Service name: " + serviceName);
        Log.d(tagNC, "Port number: " + controllerPort);
      }

      @Override
      public void onRegistrationFailed(NsdServiceInfo serviceInfo,
                                       int errorCode) {
        Log.d(tagNC, "Registration Failed! Error code: " + errorCode);
      }

      @Override
      public void onServiceUnregistered(NsdServiceInfo arg0) {
        Log.d(tagNC, "NSD-Service unregistered.");
      }

      @Override
      public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
                                         int errorCode) {
        Log.d(tagNC, "NSD-Unregistration failed!" + errorCode);
      }
    };
  }

  /**
   * unregisters Service in network to reuse Session Name onDestroy()
   */
  public void tearDownNSD() {
    mNsdManager.unregisterService(mRegistrationListener);
  }

}
