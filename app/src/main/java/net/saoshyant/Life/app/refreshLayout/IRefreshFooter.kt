package net.saoshyant.Life.app.refreshLayout


internal interface IRefreshFooter : IRefreshHeaderOrFooter {
    fun onStartLoadMore()


    fun onFinishLoadMore()
}
