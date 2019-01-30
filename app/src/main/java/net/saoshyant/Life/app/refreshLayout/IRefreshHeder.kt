package net.saoshyant.Life.app.refreshLayout

internal interface IRefreshHeder : IRefreshHeaderOrFooter {
    fun onStartRefreshing()

    fun onFinishRefreshing()
}
