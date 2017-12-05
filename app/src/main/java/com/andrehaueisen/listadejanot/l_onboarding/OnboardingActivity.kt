package com.andrehaueisen.listadejanot.l_onboarding

import android.os.Bundle
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.utilities.convertDipToPixel
import com.codemybrainsout.onboarder.AhoyOnboarderActivity
import com.codemybrainsout.onboarder.AhoyOnboarderCard

/**
 * Created by andre on 12/1/2017.
 */
class OnboardingActivity: AhoyOnboarderActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setGradientBackground()
        setFinishButtonTitle(R.string.finish)

        val firstCard = getOnboardCard(R.drawable.ic_onboard_first, getString(R.string.card_one_title), getString(R.string.card_one_description))
        val secondCard = getOnboardCard(R.drawable.ic_onboard_second, getString(R.string.card_two_title), getString(R.string.card_two_description), titleTextSize = 18F)
        val thirdCard = getOnboardCard(R.drawable.ic_onboard_third, getString(R.string.card_three_title), getString(R.string.card_three_description))
        val fourthCard = getOnboardCard(R.drawable.ic_onboard_fourth, getString(R.string.card_fourth_title), getString(R.string.card_fourth_description))

        val pages = mutableListOf(firstCard, secondCard, thirdCard, fourthCard)
        setOnboardPages(pages)
    }

    private fun getOnboardCard(icon: Int, title: String,
                               description: String,
                               backgroundColor: Int = R.color.colorPrimary,
                               titleColor: Int = R.color.colorTextAndIcons,
                               descriptionColor: Int = R.color.colorTextAndIcons,
                               titleTextSize: Float = 20F,
                               descriptionTextSize: Float = 14F,
                               iconSize: Float = 168F): AhoyOnboarderCard{

        val iconWidthAndHeight = this.convertDipToPixel(iconSize).toInt()

        val ahoyOnboardCard = AhoyOnboarderCard(title, description, icon)
        ahoyOnboardCard.setBackgroundColor(backgroundColor)
        ahoyOnboardCard.setTitleColor(titleColor)
        ahoyOnboardCard.setDescriptionColor(descriptionColor)
        ahoyOnboardCard.setTitleTextSize(titleTextSize)
        ahoyOnboardCard.setDescriptionTextSize(descriptionTextSize)
        ahoyOnboardCard.setIconLayoutParams(iconWidthAndHeight, iconWidthAndHeight, 56, 8, 8, 8)

        return ahoyOnboardCard
    }

    override fun onFinishButtonPressed() {
        finish()
    }


}