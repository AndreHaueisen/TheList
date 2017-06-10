package com.andrehaueisen.listadejanot.utilities

import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView

fun ImageView.animateVectorDrawable(initialAnimation: AnimatedVectorDrawable,
                                    finalAnimation: AnimatedVectorDrawable,
                                    useInitialToFinalFlow: Boolean){

    if(drawable == initialAnimation && useInitialToFinalFlow){
        (drawable as AnimatedVectorDrawable).start()

    }else if(drawable == initialAnimation && !useInitialToFinalFlow){
        setImageDrawable(finalAnimation)
        (drawable as AnimatedVectorDrawable).start()

    }else if(drawable == finalAnimation && useInitialToFinalFlow){
        setImageDrawable(initialAnimation)
        (drawable as AnimatedVectorDrawable).start()

    }else if(drawable == finalAnimation && !useInitialToFinalFlow){
        (drawable as AnimatedVectorDrawable).start()

    }


}
