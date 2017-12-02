package com.andrehaueisen.listadejanot.l_onboarding

import android.os.Bundle
import com.andrehaueisen.listadejanot.R
import com.codemybrainsout.onboarder.AhoyOnboarderActivity
import com.codemybrainsout.onboarder.AhoyOnboarderCard

/**
 * Created by andre on 12/1/2017.
 */
class OnboardingActivity: AhoyOnboarderActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setColorBackground(R.color.colorPrimary)

        val firstCard = getOnboardCard(R.drawable.ic_onboard_one, getString(R.string.card_one_title), getString(R.string.card_one_description))
        val secondCard = getOnboardCard(R.drawable.ic_onboard_second, getString(R.string.card_two_title), getString(R.string.card_two_description))
        val thirdCard = getOnboardCard(R.drawable.ic_onboard_third, getString(R.string.card_three_title), getString(R.string.card_three_description), titleTextSize = 10)
        val fourthCard = getOnboardCard(R.drawable.ic_onboard_fourth, getString(R.string.card_fourth_title), getString(R.string.card_fourth_description), iconSize = 160)

        val pages = mutableListOf(firstCard, secondCard, thirdCard, fourthCard)
        setOnboardPages(pages)

    }

    private fun getOnboardCard(icon: Int, title: String,
                               description: String,
                               backgroundColor: Int = R.color.colorSecondaryWhite,
                               titleColor: Int = R.color.colorPrimaryText,
                               descriptionColor: Int = R.color.colorSecondaryText,
                               titleTextSize: Int = 9,
                               descriptionTextSize: Int = 7,
                               iconSize: Int = 200): AhoyOnboarderCard{

        val iconWidthAndHeight = dpToPixels(iconSize, this).toInt()

        val ahoyOnboardCard = AhoyOnboarderCard(title, description, icon)
        ahoyOnboardCard.setBackgroundColor(backgroundColor)
        ahoyOnboardCard.setTitleColor(titleColor)
        ahoyOnboardCard.setDescriptionColor(descriptionColor)
        ahoyOnboardCard.setTitleTextSize(dpToPixels(titleTextSize, this))
        ahoyOnboardCard.setDescriptionTextSize(dpToPixels(descriptionTextSize, this))
        ahoyOnboardCard.setIconLayoutParams(iconWidthAndHeight, iconWidthAndHeight, 32, 8, 8, 8)

        return ahoyOnboardCard
    }

    override fun onFinishButtonPressed() {
        finish()
    }


}