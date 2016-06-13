/* Sprite Batcher V1.31
   Copyright (c) 2013 Tim Wicksteed <tim@twicecircled.com>
   http:/www.twicecircled.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package dasolma.com.asaplib.drawing.opengl.spritebatcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BasicTexture extends Texture {
	// Basic texture for drawing sprites

	public BasicTexture(int bitmapId) {
		this.bitmapId = bitmapId;
	}

	@Override
	protected Bitmap getBitmap(Context context) {
		return BitmapFactory.decodeResource(context.getResources(), bitmapId);
	}
}
