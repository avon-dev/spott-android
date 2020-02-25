package com.avon.spott.Main

import android.animation.ObjectAnimator
import android.content.Intent
import android.util.SparseArray
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.avon.spott.Utils.logd
import com.avon.spott.betterSmoothScrollToPosition
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_mypage.*
import kotlinx.android.synthetic.main.fragment_scrap.*

fun BottomNavigationView.setupWithNavController(navGraphIds: List<Int>, fragmentManager: FragmentManager,
                                                containerId: Int) {

    val TAG = "NavigationExtensions"

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
            logd(TAG, "[[[NE]]] newlySelectedItemTag : " +  graphIdToTagMap[item.itemId])

            if (selectedItemTag != newlySelectedItemTag) {

                //홈을 제외한 선택되었던 다른 백스택을 다 날려버린다.
                fragmentManager.popBackStack(firstFragmentTag,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE)

                val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                        as NavHostFragment

                if (firstFragmentTag != newlySelectedItemTag) {

                    fragmentManager.beginTransaction()
/*   <버그 해결> 2020-01-28
버그 : 1. (매우 빠르게) 하단네비게이션 다른 탭 눌렀다가 다시 원래있던 네비게이션 탭을 눌렀을 때, 화면에 아무것도 안나오는 현상.
      2. 로그를 찍어보니, 기존 플래그먼트의 onDestroyView가 안 실행되고 onStop까지만 진행됨. 다시 생성될 때도 onCreateView가 아닌 onStart부터 시작됨.
      3. 1번과정을 느리게 하니 onDestroyView가 한박자 늦게 실햄됨 확인.
해결 : 하단네비게이션 탭 변환시 애니메이션 전환 효과를 없앰.
*            */
//                        .setCustomAnimations(
//                            R.anim.nav_default_enter_anim,
//                            R.anim.nav_default_exit_anim,
//                            R.anim.nav_default_pop_enter_anim,
//                            R.anim.nav_default_pop_exit_anim
//                        )
                        .attach(selectedFragment)
                        .setPrimaryNavigationFragment(selectedFragment)
                        .apply {
                            // Detach all other Fragments
                            graphIdToTagMap.forEach { _, fragmentTagIter ->
                                if (fragmentTagIter != newlySelectedItemTag) {
                                    detach(fragmentManager.findFragmentByTag(firstFragmentTag)!!)
                                    logd(TAG, "[[[NE]]] detached all other Fragments")
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
                logd(TAG, "[[[NE]]] before true")
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
        logd(TAG, "[[[NE]]] addOnBackStackChangedListener")
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

            //리사이클러뷰있는 스크랩 플래그먼트면 리사이클러뷰 스크롤 UP, (아이템 없어도 터지지 않음.)
            if(selectedFragment.tag== "bottomNavigation#0"){
                if(selectedFragment.recycler_home_f!=null) {
                    selectedFragment.recycler_home_f.betterSmoothScrollToPosition(0)
                }

            }else if(selectedFragment.tag== "bottomNavigation#1"){
                if( selectedFragment.recycler_maplist_f !=null){
                    selectedFragment.recycler_maplist_f.betterSmoothScrollToPosition(0)
                }


            }else if(selectedFragment.tag == "bottomNavigation#2"){
                if( selectedFragment.scroll_scrap_f !=null){
                    selectedFragment.scroll_scrap_f.smoothScrollTo(0,0)
                }

            }else if(selectedFragment.tag == "bottomNavigation#3"){
                if(selectedFragment.recycler_grid_mypage_f !=null){
                    selectedFragment.recycler_grid_mypage_f.smoothScrollToPosition(0)
                }

            }

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