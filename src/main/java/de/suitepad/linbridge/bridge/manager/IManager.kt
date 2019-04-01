package de.suitepad.linbridge.bridge.manager

import de.suitepad.linbridge.api.SIPConfiguration


interface IManager {

    /**
     * initialize the SIP manager, this method has to be called once before trying to use the SIP client
     */
    fun start()

    /**
     * destroys the SIP client, the method has to be called on exit
     */
    fun destroy()

    /**
     * configures the SIP client using a [SIPConfiguration], pass null to reset to default configuration
     */
    fun configure(settings: SIPConfiguration?)

    /**
     * authenticates and connects to the SIP server
     * @param host SIP server hostname or ip address
     * @param port SIP server port number, default is 5060
     * @param username SIP account username used for authentication
     * @param password SIP account password used for authentication
     * @param proxy [optional] proxy server address
     */
    fun authenticate(host: String, port: Int = 5060, username: String, password: String, proxy: String?)

}