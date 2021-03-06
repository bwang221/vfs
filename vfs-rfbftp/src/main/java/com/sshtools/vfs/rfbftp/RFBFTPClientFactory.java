package com.sshtools.vfs.rfbftp;

import javax.net.SocketFactory;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

import com.sshtools.rfb.RFBContext;
import com.sshtools.rfb.RFBEncoding;
import com.sshtools.rfb.RFBEventHandler;

public class RFBFTPClientFactory {
	static SocketFactory socketFactory = null;

	public static RFBFTPClient createConnection(String hostname, int port, final String password,
			final FileSystemOptions fileSystemOptions) throws FileSystemException {
		// The file system options may already have a client
		RFBFTPClient rfb = RFBFTPFileSystemConfigBuilder.getInstance().getClient(fileSystemOptions);
		if (rfb != null) {
			return rfb;
		}
		/**
		 * TODO: use the FileSystemOptions variable to retrieve some SSH context
		 * settings
		 */
		try {
			RFBContext context = new RFBContext();
			if (port < 5800) {
				port += 5900;
			}
			rfb = new RFBFTPClient(context, hostname, port, new RFBEventHandler() {
				
				@Override
				public void remoteResize(int arg0, int arg1) {
				}

				public String passwordAuthenticationRequired() {
					if (password == null) {
						UserAuthenticator ua = DefaultFileSystemConfigBuilder.getInstance()
								.getUserAuthenticator(fileSystemOptions);
						UserAuthenticationData data = ua.requestAuthentication(
								new UserAuthenticationData.Type[] { UserAuthenticationData.PASSWORD });

						try {
							if (data == null) {
								return null;
							}
							char[] pw = data.getData(UserAuthenticationData.PASSWORD);
							if (pw == null)
								return null;
							return new String(pw);
						} finally {
							UserAuthenticatorUtils.cleanup(data);
						}
					}
					return password;
				}

				public void encodingChanged(RFBEncoding currentEncoding) {
				}

				public void disconnected() {
				}

				public void connected() {
				}
			});
		} catch (FileSystemException fse) {
			throw fse;
		} catch (final Exception ex) {
			throw new FileSystemException("vfs.provider.rfb/connect.error", ex, hostname);
		}
		return rfb;
	}
}
