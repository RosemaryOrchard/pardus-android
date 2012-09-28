/*
 *    Copyright 2012 Werner Bayer
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.pardus.android.browser;

import android.graphics.Picture;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import at.pardus.android.browser.PardusWebView.RenderStatus;

/**
 * PictureListener implementation determining when a page has finished rendering
 * and is ready to be scrolled.
 */
public class PardusPictureListener implements PictureListener {

	private static final int MAX_MS_BETWEEN_RENDER = 3000;

	private String lastUrl;

	private long lastRenderTime;

	private int lastScrollRangeX;

	private int lastScrollRangeY;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.webkit.WebView.PictureListener#onNewPicture(android.webkit.WebView
	 * , android.graphics.Picture)
	 */
	@Override
	public void onNewPicture(WebView view, Picture picture) {
		PardusWebView pardusView = (PardusWebView) view;
		if (PardusConstants.DEBUG) {
			Log.v(this.getClass().getSimpleName(),
					"New webview content rendered ("
							+ pardusView.getRenderStatus().toString()
							+ ");  current scroll range "
							+ pardusView.computeHorizontalScrollRange() + "/"
							+ pardusView.computeVerticalScrollRange());
		}
		// determine rendering stage
		int scrollRangeX = pardusView.computeHorizontalScrollRange();
		int scrollRangeY = pardusView.computeVerticalScrollRange();
		if (scrollRangeX <= Pardus.displayWidthDp
				&& scrollRangeY <= Pardus.displayHeightDp) {
			return;
		}
		String url = pardusView.getUrl();
		RenderStatus renderStatus = pardusView.getRenderStatus();
		long time = System.currentTimeMillis();
		if (renderStatus == RenderStatus.RENDER_FINISH) {
			if (url.equals(lastUrl)) {
				if (time - lastRenderTime > MAX_MS_BETWEEN_RENDER) {
					return;
				}
				if (scrollRangeX <= lastScrollRangeX
						&& scrollRangeY <= lastScrollRangeY) {
					return;
				}
			}
		} else if (renderStatus != RenderStatus.LOAD_FINISH) {
			return;
		}
		// this may be the final rendering of this page
		lastScrollRangeX = scrollRangeX;
		lastScrollRangeY = scrollRangeY;
		lastUrl = url;
		lastRenderTime = time;
		pardusView.propertiesAfterPageRender(scrollRangeX, scrollRangeY);
	}

}
