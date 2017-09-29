/**
 * 
 * JAnki is a simple library to use Anki Overdrive with Java.
 * 
 * <p>
 * JAnki uses <a href="https://github.com/intel-iot-devkit/tinyb" target="_blank">TinyB</a>
 * which you have to install.
 * 
 * <p>
 * TinyB uses BlueZ, a Linux Bluetooth stack, which you also have to install. Usually
 * included in your Linux distribution.
 * 
 * <p>
 * If you have installed TinyB carefully, you have been followed the following advice:
 * 
 * <blockquote>
 * TinyB requires CMake 3.1+ for building and requires GLib/GIO 2.40+.  
 * It also requires BlueZ with GATT profile activated, which is currently  
 * experimental (as of BlueZ 5.37), so you might have to run bluetoothd  
 * with the -E flag. For example, on a system with systemd (Fedora, poky, etc.)  
 * edit the bluetooth.service file (usually found in /usr/lib/systemd/system/  
 * or /lib/systemd/system) and append -E to ExecStart line, restart the  
 * daemon with systemctl restart bluetooth. 
 * </blockquote>
 * 
 * 
 * @author bernd
 *
 */
package de.pdbm.janki.ble;