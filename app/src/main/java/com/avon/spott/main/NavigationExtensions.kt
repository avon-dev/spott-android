package com.avon.spott.main

import android.content.Intent
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.avon.spott.R
import com.google.android.material.bottomnavigation.BottomNavigationView

fun BottomNavigationView.setupWithNavController(navGraphIds: List<Int>, fragmentManager: FragmentManager,
                                                containerId: Int, intent: Intent) {

    val graphIdToTagMap = SparseArray<String>()

    val selectedNavController = MutableLiveData<NavController>()

    var firstFragmentGraphId = 0

    //각각의 navGraphID들에 대해서 순차적으로 인텍스를 제공하며, 지정된 작업을 수행
    navGraphIds.forEachIndexed { index, navGraphId ->

        val fragmentTag = getFragmentTag(index)

        //Navigation host fragment를 찾고, 없으면 생성한다.
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            navGraphId,
            containerId
        )

        val graphId = navHostFragment.navController.graph.id

        if (index == 0) {
            firstFragmentGraphId = graphId
        }

        graphIdToTagMap[graphId] = fragmentTag

        if (this.selectedItemId == graphId) {
            selectedNavController.value = navHostFragment.navController
            attachNavHostFragment(fragmentManager, navHostFragment, index == 0)
        } else {
            detachNavHostFragment(fragmentManager, navHostFragment)
        }

    }

    var selectedItemTag = graphIdToTagMap[this.selectedItemId]

    val firstFragmentTag = graphIdToTagMap[firstFragmentGraphId]
    var isOnFirstFragment = selectedItemTag == firstFragmentTag

    //네비게이션 아이템 선택했을 때
    setOnNavigationItemSelectedListener { item ->

        if (fragmentManager.isStateSaved) {
            false
        } else {
            val newlySelectedItemTag = graphIdToTagMap[item.itemId]


            if (selectedItemTag != newlySelectedItemTag) {

                //홈을 제외한 선택되었던 다른 백스택을 다 날려버린다.
                fragmentManager.popBackStack(firstFragmentTag,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE)

                val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                        as NavHostFragment

                if (firstFragmentTag != newlySelectedItemTag) {

                    fragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.nav_default_enter_anim,
                            R.anim.nav_default_exit_anim,
                            R.anim.nav_default_pop_enter_anim,
                            R.anim.nav_default_pop_exit_anim
                        )
                        .attach(selectedFragment)
                        .setPrimaryNavigationFragment(selectedFragment)
                        .apply {
                            // Detach all other Fragments
                            graphIdToTagMap.forEach { _, fragmentTagIter ->
                                if (fragmentTagIter != newlySelectedItemTag) {
                                    detach(fragmentManager.findFragmentByTag(firstFragmentTag)!!)
                                    println(" 10. fragmentTagIter 디테치드!!!   :  "+ fragmentTagIter)
                                }
                            }
                        }
                        .addToBackStack(firstFragmentTag)
                        .setReorderingAllowed(true)
                        .commit()
                }
                selectedItemTag = newlySelectedItemTag
                isOnFirstFragment = selectedItemTag == firstFragmentTag
                selectedNavController.value = selectedFragment.navController
                true
            } else {
                false
            }
        }
    }

    // 선택된 아이템을 또 선택했을 때 일어나는 일들 처리
    setupItemReselected(graphIdToTagMap, fragmentManager)

    // 백스택 변화에 따른 BottomNavigationView의 아이템 처리
    fragmentManager.addOnBackStackChangedListener {

        if (!isOnFirstFragment && !fragmentManager.isOnBackStack(firstFragmentTag)) {
            this.selectedItemId = firstFragmentGraphId
        }

        selectedNavController.value?.let { controller ->
            if (controller.currentDestination == null) {
                controller.navigate(controller.graph.id)
            }
        }
    }

}


private fun BottomNavigationView.setupItemReselected(graphIdToTagMap: SparseArray<String>,
                                                     fragmentManager: FragmentManager) {
    setOnNavigationItemReselectedListener { item ->
        val newlySelectedItemTag = graphIdToTagMap[item.itemId]
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                as NavHostFragment
        val navController = selectedFragment.navController


        if(navController.currentDestination?.id!=navController.graph.startDestination) {
            // Pop the back stack to the start destination of the current navController graph
            navController.popBackStack(
                navController.graph.startDestination, false
            )

        }else{

            //현재 보고있는 프래그먼트가 첫번째 프래그먼트면
//            if(selectedFragment.tag== "bottomNavigation#2"){ //스크롤있는 스크랩 플래그먼트면 스크롤업
//                selectedFragment.scollview.smoothScrollTo(0,0)
//            }

        }


    }
}

private fun detachNavHostFragment(fragmentManager: FragmentManager, navHostFragment: NavHostFragment) {
    fragmentManager.beginTransaction()
        .detach(navHostFragment)
        .commitNow()
}

private fun attachNavHostFragment(fragmentManager: FragmentManager, navHostFragment: NavHostFragment,
                                  isPrimaryNavFragment: Boolean) {
    fragmentManager.beginTransaction()
        .attach(navHostFragment)
        .apply {
            if (isPrimaryNavFragment) {
                setPrimaryNavigationFragment(navHostFragment)
            }
        }
        .commitNow()

}

private fun obtainNavHostFragment(fragmentManager: FragmentManager, fragmentTag: String,
                                  navGraphId: Int, containerId: Int): NavHostFragment {
    // If the Nav Host fragment exists, return it
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    existingFragment?.let { return it }

    // Otherwise, create it and return it.
    val navHostFragment = NavHostFragment.create(navGraphId)
    fragmentManager.beginTransaction()
        .add(containerId, navHostFragment, fragmentTag)
        .commitNow()
    return navHostFragment
}

fun FragmentManager.isOnBackStack(backStackName: String): Boolean {

    val backStackCount = backStackEntryCount

    for (index in 0 until backStackCount) {
        if (getBackStackEntryAt(index).name == backStackName) {
            return true
        }
    }
    return false
}

private fun getFragmentTag(index: Int) = "bottomNavigation#$index"