import React from "react";

import { Fragment } from "react";
import MainNavigation from "./MainNavigation";
import SubNavigation from "./SubNavigation"

type Props = {
  children?: React.ReactNode
}

const Layout: React.FC<Props> = (props) => {


  return (
    <Fragment>
      <MainNavigation/>
      <SubNavigation/>
      <main>{props.children}</main>
    </Fragment>
  )
};

export default Layout;